package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.AbstractMessageResponder;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.RequestHeader;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;

@Service("BlockFullSyncResponder")
public class BlockSyncResponder extends AbstractMessageResponder<Block> implements MessageResponder<MessagePayload>{
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("SyncBlockId_Cache")
  private Cache<String, String> syncBlockIdCache;
  
  @Value("${atomix.node-id}")
  private String nodeId;
    
  protected SyncResponse<Block> createResponse(SyncRequest request) {
    List<Block> blocks = blockService.findBlocks(request.getStartingPosition(), request.getRequestPackageSize());
    SyncResponse<Block> response = new SyncResponse<>();
    
    if(areBlocksMatchingRequestHeaders(blocks, request.getWantedHeaders())) {
      response.setEntities(blocks);
      response.setLastPositionReached(blocks.size() < request.getRequestPackageSize());
    } else {
      response.setEntities(new ArrayList<>());
      response.setLastPositionReached(false);
    }
    response.setLastPositionReached(blocks.size() < request.getRequestPackageSize());
    response.setStartingPosition(request.getStartingPosition());
    
    return response;
  }
  
  //TODO check if it's OK to return new entries which are not in the headers listed? for now I'll settle with yes!
  private boolean areBlocksMatchingRequestHeaders(List<Block> blocks, List<RequestHeader> headers) {
    return CollectionUtils.isNotEmpty(headers) && headers.stream().allMatch(header -> {
      var possibleBlock = blocks.stream().filter(block -> block.getPosition() == header.getPosition()).findFirst();
      if(possibleBlock.isPresent()) {
        return StringUtils.equalsIgnoreCase(possibleBlock.get().getHash(), header.getHash());
      } else {
        return false;
      }
    });
  }

  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.SYNC_BLOCKCHAIN;
  }

  @Override
  protected Cache<String, String> getCache() {
    return syncBlockIdCache;
  }

  @Override
  protected String getNodeId() {
    return nodeId;
  }
}
