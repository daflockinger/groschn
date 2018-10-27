package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.AbstractMessageResponder;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;

@Service("BlockFullSyncResponder")
public class BlockSyncResponder extends AbstractMessageResponder<Block> {
  
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
    response.setEntities(blocks);
    response.setLastPositionReached(blocks.size() < request.getRequestPackageSize());
    response.setStartingPosition(request.getStartingPosition());
    
    return response;
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
