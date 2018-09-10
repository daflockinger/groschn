package com.flockinger.groschn.blockchain.blockworks.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.messaging.MessageReceiverUtils;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageListener;
import com.flockinger.groschn.messaging.inbound.SubscriptionService;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class FreshBlockListener implements MessageListener<MessagePayload> {

  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private MessageReceiverUtils messageUtils;
  
  @Autowired
  @Qualifier("BlockId_Cache")
  private Cache<String, String> blockIdCache;
  
  private final static Logger LOG = LoggerFactory.getLogger(FreshBlockListener.class);
  
  @Autowired
  public FreshBlockListener(SubscriptionService<MessagePayload> subscriptionService) {
    subscriptionService.subscribe(this);
  }
  
  @Override
  public void receiveMessage(Message<MessagePayload> message) {
    try {
      messageUtils.assertEntity(message);
      assertMessageIsNew(message.getId());
      Optional<Block> block = messageUtils.extractPayload(message, Block.class);
      if (block.isPresent()) {
        LOG.info("Fresh Block received by sender: " + message.getPayload().getSenderId());
        blockService.saveInBlockchain(block.get());
      }
    } catch (BlockchainException e) {
      LOG.error("Invalid Block-Message received: " + e.getMessage(), e);
    }
  }
  
  private void assertMessageIsNew(String messageId) {
    Optional<String> existingKey = Optional.ofNullable(blockIdCache.getIfPresent(messageId));
    if(!existingKey.isPresent()) {
      blockIdCache.put(messageId, messageId);
    } else {
      throw new ReceivedMessageInvalidException("Block Message was already received with ID: " + messageId);
    }
  }
  
  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.FRESH_BLOCK;
  }
}
