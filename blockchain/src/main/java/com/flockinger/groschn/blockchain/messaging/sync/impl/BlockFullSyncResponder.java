package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class BlockFullSyncResponder implements MessageResponder<MessagePayload>{
  
  @Autowired
  private NetworkStatistics networkStatistics;
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private MessagingUtils messageUtils;
  @Autowired
  @Qualifier("SyncBlockId_Cache")
  private Cache<String, String> syncBlockIdCache;
  
  @Value("${blockchain.node.id}")
  private String nodeId;
  
  private final static Logger LOG = LoggerFactory.getLogger(BlockFullSynchronizer.class);
  private final static Long BLOCK_REQUEST_PACKAGE_SIZE = 10l;

  @Override
  public Optional<Message<MessagePayload>> respond(Message<MessagePayload> request) {
    Optional<Message<MessagePayload>> response = Optional.empty();
    try {
      messageUtils.assertEntity(request);
      isMessageFromALegitSender(request.getPayload().getSenderId());
      assertMessageIsNew(request.getId());
      Optional<SyncRequest> syncRequest = messageUtils.extractPayload(request, SyncRequest.class);
      if (syncRequest.isPresent() && isRequestValid(syncRequest.get())) {
        response = Optional.ofNullable(createResponse(syncRequest.get()));
      }
    } catch (BlockchainException e) {
      LOG.error("Invalid Block-Syncing-Request received: " + e.getMessage(), e);
    }
    return response;
  }
  
  private void assertMessageIsNew(String messageId) {
    Optional<String> existingKey = Optional.ofNullable(syncBlockIdCache.getIfPresent(messageId));
    if(!existingKey.isPresent()) {
      syncBlockIdCache.put(messageId, messageId);
    } else {
      throw new ReceivedMessageInvalidException("Block-Syncing-Request was already received with ID: " + messageId);
    }
  }
  private void isMessageFromALegitSender(String senderId) {
    boolean doesSenderExist = networkStatistics.activeNodeIds().stream()
        .anyMatch(nodeId -> StringUtils.equals(nodeId, senderId));
    if(!doesSenderExist) {
      throw new ReceivedMessageInvalidException("Sender is not existing in network with ID: " + senderId);
    }
  }
  
  private boolean isRequestValid(SyncRequest request) {
    return request.getStartingPosition() != null && request.getStartingPosition() > 0;
  }
  
  private Message<MessagePayload> createResponse(SyncRequest request) {
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
}
