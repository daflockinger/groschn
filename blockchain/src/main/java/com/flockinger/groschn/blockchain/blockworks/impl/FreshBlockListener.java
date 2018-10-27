package com.flockinger.groschn.blockchain.blockworks.impl;

import static com.flockinger.groschn.blockchain.validation.AssessmentFailure.BLOCK_LAST_HASH_WRONG;
import static com.flockinger.groschn.blockchain.validation.AssessmentFailure.BLOCK_POSITION_TOO_HIGH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.validation.AssessmentFailure;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.AbstractMessageListener;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class FreshBlockListener extends AbstractMessageListener<Block> {

  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private SyncDeterminator blockSyncDeterminator;
  
  @Autowired
  @Qualifier("BlockId_Cache")
  private Cache<String, String> blockIdCache;
  
  private final static Logger LOG = LoggerFactory.getLogger(FreshBlockListener.class);
  
  @Override
  protected void handleMessage(Block block) {
    try {
      blockService.saveInBlockchain(block);
    } catch (AssessmentFailedException e) {
      LOG.error("Invalid Block-Message received maybe recoverable: " + e.getMessage(), e);
      // if the validation failed due to an outdated chain or or some faulty blocks in the chain
      // try to Resynchronize the Blockchain.
      if(isSynchronizationRecoverable(e.getFailure())) {
        blockSyncDeterminator.determineAndSync();
      }
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
  @Override
  protected Cache<String, String> getCache() {
    return blockIdCache;
  }
  @Override
  protected Class<Block> messageBodyType() {
    return Block.class;
  }
}
