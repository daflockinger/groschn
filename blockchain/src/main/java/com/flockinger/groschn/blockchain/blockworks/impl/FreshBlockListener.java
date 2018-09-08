package com.flockinger.groschn.blockchain.blockworks.impl;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageListener;
import com.flockinger.groschn.messaging.inbound.SubscriptionService;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class FreshBlockListener implements MessageListener<MessagePayload> {

  @Autowired
  private CompressionUtils compressor;
  @Autowired
  private BlockStorageService blockService;
  
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
        Block block = extractBlock(message);
        blockService.saveInBlockchain(block);
      } catch (BlockchainException e) {
        LOG.error("Invalid Block-Message received: " + e.getMessage(), e);
      }  
  }
  
  private Block extractBlock(Message<MessagePayload> message) {
    Optional<MessagePayload> receivedBlockMessage = Optional.empty();
    if(isMessageMetaDataValid(message)) {
      receivedBlockMessage = Optional.ofNullable(message.getPayload());
    }
    return receivedBlockMessage.stream().map(this::helloAndCheckSender).filter(Objects::nonNull)
        .map(MessagePayload::getEntity).filter(Objects::nonNull)
        .map(this::decompressBlock).filter(Optional::isPresent)
        .map(Optional::get).findFirst().orElseThrow(() -> new ReceivedMessageInvalidException(
            "No viable and new Block available in the Message!"));
  }
  
  private boolean isMessageMetaDataValid(Message<MessagePayload> message) {
    var now = new Date();
    return message.getTimestamp() != null && isNotEmpty(message.getId()) && 
        isMessageNew(message.getId()) && now.getTime() > message.getTimestamp();
  }
  
  private boolean isMessageNew(String messageId) {
    Optional<String> existingKey = Optional.ofNullable(blockIdCache.getIfPresent(messageId));
    if(!existingKey.isPresent()) {
      blockIdCache.put(messageId, messageId);
    } else {
      LOG.warn("Block Message was already received with ID: " + messageId);
    }
    return !existingKey.isPresent();
  }
  
  private MessagePayload helloAndCheckSender(MessagePayload message) {
    if(isNotEmpty(message.getSenderId())) {
      LOG.info("Fresh Block received by sender: " + message.getSenderId());
      return message;
    } else {
      LOG.error("Cannot receive message from unknown Sender!");
      return null;
    }
  }
  
  private Optional<Block> decompressBlock(CompressedEntity compressedBlock) {
    if(ArrayUtils.isNotEmpty(compressedBlock.getEntity()) && compressedBlock.getOriginalSize() > 0) {
      return compressor.decompress(compressedBlock.getEntity(), compressedBlock.getOriginalSize(), Block.class);
    }
    return Optional.empty();
  } 

  @Override
  public String getSubscribedTopic() {
    return MainTopics.FRESH_BLOCK.name();
  }
}
