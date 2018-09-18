package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.GeneralMessageResponder;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;

@Service("BlockFullSyncResponder")
public class BlockSyncResponder extends GeneralMessageResponder {
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private MessagingUtils messageUtils;
  @Autowired
  @Qualifier("SyncBlockId_Cache")
  private Cache<String, String> syncBlockIdCache;
  
  @Value("${blockchain.node.id}")
  private String nodeId;
  
  private final static int BLOCK_REQUEST_PACKAGE_SIZE = 10;
  
  protected Message<MessagePayload> createResponse(SyncRequest request) {
    List<Block> blocks = blockService.findBlocks(request.getStartingPosition(), BLOCK_REQUEST_PACKAGE_SIZE);
    SyncResponse<Block> response = new SyncResponse<>();
    response.setEntities(blocks);
    response.setLastPositionReached(blocks.size() < BLOCK_REQUEST_PACKAGE_SIZE);
    response.setStartingPosition(request.getStartingPosition());
    
    return messageUtils.packageMessage(response, nodeId);
  }

  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.SYNC_BLOCKCHAIN;
  }

  @Override
  protected Cache<String, String> getCache() {
    return syncBlockIdCache;
  }
}
