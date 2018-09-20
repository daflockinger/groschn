package com.flockinger.groschn.blockchain.blockworks.impl;

import static com.flockinger.groschn.blockchain.validation.AssessmentFailure.BLOCK_LAST_HASH_WRONG;
import static com.flockinger.groschn.blockchain.validation.AssessmentFailure.BLOCK_POSITION_TOO_HIGH;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.validation.AssessmentFailure;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageListener;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class FreshBlockListener implements MessageListener<MessagePayload> {

  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private MessagingUtils messageUtils;
  @Autowired
  private SyncDeterminator blockSyncDeterminator;
  
  @Autowired
  @Qualifier("BlockId_Cache")
  private Cache<String, String> blockIdCache;
  
  private final static Logger LOG = LoggerFactory.getLogger(FreshBlockListener.class);
  
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
    } catch (AssessmentFailedException e) {
      LOG.error("Invalid Block-Message received maybe recoverable: " + e.getMessage(), e);
      // if the validation failed due to an outdated chain or or some faulty blocks in the chain
      // try to Resynchronize the Blockchain.
      if(isSynchronizationRecoverable(e.getFailure())) {
        blockSyncDeterminator.determineAndSync();
      }
    } catch (BlockchainException e) {
      LOG.error("Really invalid Block-Message received: " + e.getMessage(), e);
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
  
  private boolean isSynchronizationRecoverable(AssessmentFailure failure) {
    return failure != null && failure.equals(BLOCK_LAST_HASH_WRONG) || 
        failure.equals(BLOCK_POSITION_TOO_HIGH);
  }
  
  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.FRESH_BLOCK;
  }
}
