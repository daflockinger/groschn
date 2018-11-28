package com.flockinger.groschn.messaging.sync;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SyncInquirerImpl implements SyncInquirer {

  @Autowired
  private NetworkStatistics networkStatistics;
  @Autowired
  private ConcurrentMessenger messenger;
  @Autowired
  private MessagingUtils utils;
  @Autowired
  private SyncRequester requester;
  
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
      response = fetchMessages(request);
    }
    return response;
  }

  private void addNodesIfNeeded(SyncBatchRequest request) {
    if(isEmpty(request.getSelectedNodeIds())) {
      request.selectedNodeIds(networkStatistics.activeNodeIds().stream()
          .filter(id -> !id.equals(nodeId)).collect(toList()));
    }
  }


  private <T extends Hashable<T>> List<SyncResponse<T>> fetchMessages(SyncBatchRequest request) {
    var syncPartners = determineSyncPartners(request.getIdealReceiveNodeCount() * 2, request.getSelectedNodeIds());
    var requests = new ArrayList<>(syncPartners.stream()
        .collect(Collectors.toMap(Function.identity(), it -> request))
        .entrySet());

    return messenger.fetch(requests, requester::doRequest).stream()
        .flatMap(Optional::stream)
        .map(it -> (SyncResponse<T>)it)
        .collect(Collectors.toList());
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
}
