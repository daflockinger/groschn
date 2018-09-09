package com.flockinger.groschn.blockchain.messaging.sync.impl;

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
import java.util.concurrent.ExecutorService;
import static java.util.stream.Collectors.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.messaging.MessageReceiverUtils;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class BlockFullSyncronizer implements SyncKeeper {

  @Autowired
  private Broadcaster<MessagePayload> broadcaster;
  @Autowired
  private NetworkStatistics networkStatistics;
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private MessageReceiverUtils messageUtils;
  @Autowired
  @Qualifier("SyncBlockId_Cache")
  private Cache<String, String> syncBlockIdCache;
  @Autowired
  private MerkleRootCalculator merkleCalculator;
  
  @Value("${blockchain.node.id}")
  private String nodeId;
  
  private final static Logger LOG = LoggerFactory.getLogger(BlockFullSyncronizer.class);
 
  private final static Long BLOCK_REQUEST_PACKAGE_SIZE = 10l;
  public final static Long IDEAL_RECEIVE_NODE_COUNT = 3l;
  private final static Long RANDOM_SELECTION_SIZE = IDEAL_RECEIVE_NODE_COUNT * 2;
  
  private final static Integer BLOCK_FETCH_MAX_RETRIES = 3;
  
  public SecureRandom randomizer = new SecureRandom();
  
  @Override
  public void syncronize(Long fromPosition) {
    
    
    
    Optional<List<Block>> blocks = findBlocks(fromPosition);
    //TODO continue
    //TODO don't forget the isDone flag!!
    
  }
  
  private Optional<List<Block>> findBlocks(Long fromPosition) {
    Optional<List<Block>> blocks = Optional.empty();
    for(int retryCount=0; retryCount < BLOCK_FETCH_MAX_RETRIES && !blocks.isPresent(); retryCount ++) {
      List<Message<MessagePayload>> messageBatch = fetchMessages(fromPosition);
      blocks = findMostLikelyValidBlocks(messageBatch);
    }
    return blocks;
  }
  
  private List<Message<MessagePayload>> fetchMessages(Long fromPosition) {
    var syncPartners = getSyncPartners();
    var minResultsNeeded = (syncPartners.size() >= 3) ? 3 : 1; 
    MessagePayload payload = createPayload(fromPosition);
    var runningSyncRequests = syncPartners.stream()
        .map(id -> sendMessageAsync(id, payload)).collect(toList());
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
      LOG.error("Requesting block sync package failed(probably timeout or offline node): ",exception); 
      return req;})).forEach(CompletableFuture::join);
  }
    
  private void cancelRunningRequests(List<CompletableFuture<Message<MessagePayload>>> runningRequests) {
    runningRequests.stream().filter(this::isRunning).forEach(fut -> fut.cancel(true));;
  }
  private boolean isRunning(CompletableFuture<?> future) {
    return !(future.isDone() || future.isCompletedExceptionally() || future.isCancelled());
  }
  
  private List<String> getSyncPartners() {
    var nodeIds = networkStatistics.activeNodeIds().stream()
        .filter(id -> !id.equals(nodeId)).collect(toList());;
    if(networkStatistics.activeNodeCount() < RANDOM_SELECTION_SIZE) {
      return nodeIds;
    } else {
      return selectRandomNodes(nodeIds);
    }
  }
  
  private List<String> selectRandomNodes(List<String> activeNodes) {
    List<String> selectedNodes = new ArrayList<>();
    
    for(int selectCount=0; selectCount < RANDOM_SELECTION_SIZE; selectCount++) {
      var selectedNode = activeNodes.remove(randomizer.nextInt(activeNodes.size()));
      selectedNodes.add(selectedNode);
    }
    return selectedNodes;
  }
  
  private MessagePayload createPayload(Long startingPosition) {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(startingPosition);
    return messageUtils.packageMessage(null, nodeId).getPayload();
  }
  
  private CompletableFuture<Message<MessagePayload>> sendMessageAsync(String receiverId, MessagePayload payload) {
    Message<MessagePayload> message = new Message<>();
    message.setPayload(payload);
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(new Date().getTime());
    return broadcaster.sendRequest(message, receiverId, MainTopics.SYNC_BLOCKCHAIN);
  }
  
  
  private Optional<List<Block>> findMostLikelyValidBlocks(List<Message<MessagePayload>> messages) {
    return messages.stream()
        .map(message -> messageUtils.extractPayload(message, SyncResponse.class))
        .filter(Optional::isPresent).map(Optional::get).map(response -> (SyncResponse<Block>)response)
        .filter(this::areHashesCorrect).collect(
            groupingBy(this::merkleRootOfBlockHashes, mapping(SyncResponse::getEntities, toList())))
            .entrySet().stream().map(Entry::getValue)
            .reduce(this::findMajorlyAcceptedBlockBatch).stream()
            .flatMap(Collection::stream).findFirst();
  }
  
  private boolean areHashesCorrect(SyncResponse<Block> response) {
    //TODO implement
    return true;
  }
  
  private String merkleRootOfBlockHashes(SyncResponse<Block> response) {
    return merkleCalculator.calculateMerkleRootHash(response.getEntities());
  }
  
  private List<List<Block>> findMajorlyAcceptedBlockBatch(List<List<Block>> firstBatch, 
      List<List<Block>> secondBatch) {
    return (firstBatch.size() > secondBatch.size()) ? firstBatch : secondBatch;
  }
}
