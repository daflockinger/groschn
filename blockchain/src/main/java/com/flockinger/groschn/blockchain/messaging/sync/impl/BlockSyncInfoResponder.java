package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.AbstractMessageResponder;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class BlockSyncInfoResponder extends AbstractMessageResponder<BlockInfo> implements MessageResponder<MessagePayload> {
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("SyncBlockInfoId_Cache")
  private Cache<String, String> syncBlockInfoIdCache;
  
  @Value("${atomix.node-id}")
  private String nodeId;
    
  protected SyncResponse<BlockInfo> createResponse(SyncRequest request) {
    List<BlockInfo> infos = blockService.findBlocks(request.getStartingPosition(), request.getRequestPackageSize())
        .stream().map(this::mapToBlockInfo).collect(Collectors.toList());
    SyncResponse<BlockInfo> response = new SyncResponse<>();
    response.setEntities(infos);
    response.setLastPositionReached(infos.size() < request.getRequestPackageSize());
    response.setStartingPosition(request.getStartingPosition());
    
    return response;
  }
  
  private BlockInfo mapToBlockInfo(Block block) {
    var blockInfo = new BlockInfo();
    blockInfo.setBlockHash(block.getHash());
    blockInfo.setPosition(block.getPosition());
    return blockInfo;
  }

  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.BLOCK_INFO;
  }

  @Override
  protected Cache<String, String> getCache() {
    return syncBlockInfoIdCache;
  }

  @Override
  protected String getNodeId() {
    return nodeId;
  }
}
