package com.flockinger.groschn.messaging.sync;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;

@Service
public class SyncInquirerImpl implements SyncInquirer {
  
  @Autowired
  private Broadcaster<MessagePayload> broadcaster;
  @Autowired
  private NetworkStatistics networkStatistics;
  @Autowired
  private ConcurrentMessenger messenger;
  @Autowired
  private MessagingUtils utils;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  public SecureRandom randomizer = new SecureRandom();
  
  
  @Override
  public <T extends Hashable<T>> List<SyncResponse<T>> fetchNextBatch(SyncBatchRequest request,
      Class<T> responseType) {
    utils.assertEntity(request);
    addNodesIfNeeded(request);
    List<SyncResponse<T>> response = new ArrayList<>();
    
    for(int retryCount=0; retryCount < request.getMaxFetchRetries() && response.isEmpty(); retryCount ++) {
      List<Message<MessagePayload>> messageBatch = fetchMessages(request);
      response = findResponses(messageBatch, responseType);
    }
    return response;
  }
  
  private void addNodesIfNeeded(SyncBatchRequest request) {
    if(isEmpty(request.getSelectedNodeIds())) {
      request.selectedNodeIds(networkStatistics.activeNodeIds().stream()
          .filter(id -> !id.equals(nodeId)).collect(toList()));
    }
  }
  
  private List<Message<MessagePayload>> fetchMessages(SyncBatchRequest request) {
    var syncPartners = determineSyncPartners(request.getIdealReceiveNodeCount() * 2, request.getSelectedNodeIds());
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
  
  private List<String> determineSyncPartners(int randomSelectionSize, List<String> nodeIds) {
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
    return utils.packageMessage(request, nodeId).getPayload();
  }
  
  private <T extends Hashable<T>> List<SyncResponse<T>> findResponses(List<Message<MessagePayload>> messages, Class<T> payloadType) {
    return messages.stream()
        .map(message -> utils.extractPayload(message, SyncResponse.class))
        .filter(Optional::isPresent).map(Optional::get).map(response -> (SyncResponse<T>)response)
        .filter(this::hasEntities)
        .collect(Collectors.toList());
  }
  
  private <T extends Hashable<T>> boolean hasEntities(SyncResponse<T> response) {
    return response.getEntities() != null && !response.getEntities().isEmpty();
  }
}
