package com.flockinger.groschn.blockchain.messaging.respond;

import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BlockSyncInfoResponder implements MessageResponder<MessagePayload> {
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("SyncBlockInfoId_Cache")
  private Cache<String, String> syncBlockInfoIdCache;
  @Autowired
  private MessagePackageHelper helper;
  
  @Value("${atomix.node-id}")
  private String nodeId;

  @Override
  public Message<MessagePayload> respond(Message<MessagePayload> request) {
    Message<MessagePayload> responseMessage = new Message<>();
    var syncRequest = helper.verifyAndUnpackRequest(request, syncBlockInfoIdCache);
    if(syncRequest.isPresent()) {
      var syncResponse = createResponse(syncRequest.get());
      responseMessage = helper.packageResponse(syncResponse, nodeId);
    }
    return responseMessage;
  }

  private SyncResponse<BlockInfo> createResponse(SyncRequest request) {
    List<BlockInfo> infos = blockService.findBlocks(request.getStartingPosition(), request.getRequestPackageSize())
        .stream().map(this::mapToBlockInfo).collect(Collectors.toList());
    SyncResponse<BlockInfo> response = new SyncResponse<>();
    response.setEntities(infos);
    response.setLastPosition(blockService.getLatestBlock().getPosition());    
    response.setStartingPosition(request.getStartingPosition());
    response.setNodeId(nodeId);

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
}
