package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static java.util.stream.Collectors.toList;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.sync.ConcurrentMessenger;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SyncInquirerImpl implements SyncInquirer {
  
  @Autowired
  private Broadcaster<MessagePayload> broadcaster;
  @Autowired
  private NetworkStatistics networkStatistics;
  @Autowired
  private MessagingUtils messageUtils;
  @Autowired
  private ConcurrentMessenger messenger;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  public SecureRandom randomizer = new SecureRandom();

  
  //TODO make findMajorlyAcceptedBatch optional and add custom Function<...> interface to SyncBatchRequest
  @Override
  public <T extends Hashable<T>> List<SyncResponse<T>> fetchNextBatch(SyncBatchRequest request,
      Class<T> responseType) {
    messageUtils.assertEntity(request);
    List<SyncResponse<T>> response = findResponses(fetchMessages(request), responseType);
    
    for(int retryCount=0; retryCount < request.getMaxFetchRetries() && response.isEmpty(); retryCount ++) {
      List<Message<MessagePayload>> messageBatch = fetchMessages(request);
      response = findResponses(messageBatch, responseType);
    }
    
    return response;
  }
  
  private List<Message<MessagePayload>> fetchMessages(SyncBatchRequest request) {
    var syncPartners = getSyncPartners(request.getIdealReceiveNodeCount() * 2);
    var requests = syncPartners.stream()
        .collect(Collectors.toMap(Function.identity(), it -> request))
        .entrySet().stream().collect(Collectors.toList());
    
    return messenger.fetch(requests, this::request);  
  }
  
  private CompletableFuture<Message<MessagePayload>> request(Entry<String, SyncBatchRequest> requestEntity) {
    var request = requestEntity.getValue();
    return broadcaster.sendRequest(createMessage(request), requestEntity.getKey(), request.getTopic());
  }
    
  private Message<MessagePayload> createMessage(SyncBatchRequest request) {
    Message<MessagePayload> message = new Message<>();
    message.setPayload(createPayload(request));
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(new Date().getTime());
    return message;
  }
  
  private List<String> getSyncPartners(int randomSelectionSize) {
    var nodeIds = networkStatistics.activeNodeIds().stream()
        .filter(id -> !id.equals(nodeId)).collect(toList());
    if(nodeIds.size() < randomSelectionSize) {
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
  
  private <T extends Hashable<T>> List<SyncResponse<T>> findResponses(List<Message<MessagePayload>> messages, Class<T> payloadType) {
    return messages.stream()
        .map(message -> messageUtils.extractPayload(message, SyncResponse.class))
        .filter(Optional::isPresent).map(Optional::get).map(response -> (SyncResponse<T>)response)
        .filter(this::hasEntities)
        .collect(Collectors.toList());
  }
  
  private <T extends Hashable<T>> boolean hasEntities(SyncResponse<T> response) {
    return response.getEntities() != null && !response.getEntities().isEmpty();
  }
}
