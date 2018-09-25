package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.MerkleRootCalculator;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;

@Service
public class SyncInquirerImpl implements SyncInquirer {
  
  @Autowired
  private Broadcaster<MessagePayload> broadcaster;
  @Autowired
  private NetworkStatistics networkStatistics;
  @Autowired
  private MessagingUtils messageUtils;
  @Autowired
  private MerkleRootCalculator merkleCalculator;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  private final static Logger LOG = LoggerFactory.getLogger(SyncInquirerImpl.class);
  public SecureRandom randomizer = new SecureRandom();

  
  //TODO make findMajorlyAcceptedBatch optional and add custom Function<...> interface to SyncBatchRequest
  @Override
  public <T extends Hashable<T>> Optional<SyncResponse<T>> fetchNextBatch(SyncBatchRequest request,
      Class<T> responseType) {
    messageUtils.assertEntity(request);
    Optional<SyncResponse<T>> response = Optional.empty();
    for(int retryCount=0; retryCount < request.getMaxFetchRetries() && !response.isPresent(); retryCount ++) {
      List<Message<MessagePayload>> messageBatch = fetchMessages(request);
      response = findMostLikelyValidResponse(messageBatch, responseType);
    }
    return response;
  }
  
  private List<Message<MessagePayload>> fetchMessages(SyncBatchRequest request) {
    var syncPartners = getSyncPartners(request.getIdealReceiveNodeCount() * 2);
    var idealNodeCount = request.getIdealReceiveNodeCount();
    var minResultsNeeded = (syncPartners.size() >= idealNodeCount) ? idealNodeCount : 1; 
    MessagePayload payload = createPayload(request);
    var runningSyncRequests = syncPartners.stream()
        .map(id -> sendMessageAsync(id, payload, request.getTopic())).collect(toList());
    var arrivedMessages = new ArrayList<Message<MessagePayload>>();
    runningSyncRequests.forEach(completableRequest -> completableRequest
        .thenApply(arrivedMessages::add)            
        .thenAccept(message -> {           
          boolean areEnoughMessagesArrived = arrivedMessages.size() >= minResultsNeeded;             
          if(areEnoughMessagesArrived) {          
            cancelRunningRequests(runningSyncRequests);              
          }}));
    blockUntilDoneAndLogErrors(runningSyncRequests);
    return arrivedMessages.stream().filter(Objects::nonNull).collect(toList());
  }
  
  private void blockUntilDoneAndLogErrors(List<CompletableFuture<Message<MessagePayload>>> requests) {
    requests.stream().map(request -> request.handle((req,exception) -> {
      LOG.error("Requesting sync package failed(probably timeout or offline node): ",exception); 
      return req;})).forEach(CompletableFuture::join);
  }
    
  private void cancelRunningRequests(List<CompletableFuture<Message<MessagePayload>>> runningRequests) {
    runningRequests.stream().filter(this::isRunning).forEach(fut -> fut.cancel(true));;
  }
  private boolean isRunning(CompletableFuture<?> future) {
    return !(future.isDone() || future.isCompletedExceptionally() || future.isCancelled());
  }
  
  private List<String> getSyncPartners(int randomSelectionSize) {
    var nodeIds = networkStatistics.activeNodeIds().stream()
        .filter(id -> !id.equals(nodeId)).collect(toList());;
    if(networkStatistics.activeNodeCount() < randomSelectionSize) {
      return nodeIds;
    } else {
      return selectRandomNodes(nodeIds, randomSelectionSize);
    }
  }
  
  private List<String> selectRandomNodes(List<String> activeNodes, int randomSelectionSize) {
    List<String> selectedNodes = new ArrayList<>();
    for(int selectCount=0; selectCount < randomSelectionSize; selectCount++) {
      var selectedNode = activeNodes.remove(randomizer.nextInt(activeNodes.size()));
      selectedNodes.add(selectedNode);
    }
    return selectedNodes;
  }
  
  private MessagePayload createPayload(SyncBatchRequest batchRequest) {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(batchRequest.getFromPosition());
    request.setRequestPackageSize(Integer.toUnsignedLong(batchRequest.getBatchSize()));
    return messageUtils.packageMessage(request, nodeId).getPayload();
  }
  
  private CompletableFuture<Message<MessagePayload>> sendMessageAsync(String receiverId, MessagePayload payload, MainTopics topic) {
    Message<MessagePayload> message = new Message<>();
    message.setPayload(payload);
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(new Date().getTime());
    return broadcaster.sendRequest(message, receiverId, topic);
  }
  
  
  private <T extends Hashable<T>> Optional<SyncResponse<T>> findMostLikelyValidResponse(List<Message<MessagePayload>> messages, Class<T> payloadType) {
    return messages.stream()
        .map(message -> messageUtils.extractPayload(message, SyncResponse.class))
        .filter(Optional::isPresent).map(Optional::get).map(response -> (SyncResponse<T>)response)
        .collect(groupingBy(this::merkleRootOfPayloadHashes, toList()))
            .entrySet().stream().map(Entry::getValue)
            .reduce(this::findMajorlyAcceptedBatch).stream()
            .flatMap(Collection::stream).findFirst();
  }
  
  private <T extends Hashable<T>> String merkleRootOfPayloadHashes(SyncResponse<T> response) {
    return merkleCalculator.calculateMerkleRootHash(response.getEntities());
  }
  
  private <T extends Hashable<T>> List<SyncResponse<T>> findMajorlyAcceptedBatch(
      List<SyncResponse<T>> first, List<SyncResponse<T>> second){
    return (first.size() > second.size()) ? first : second;
  }
}
