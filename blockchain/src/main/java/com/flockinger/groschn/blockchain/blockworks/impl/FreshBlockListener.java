package com.flockinger.groschn.blockchain.blockworks.impl;

import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.RESTART;
import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.STOP;
import static com.flockinger.groschn.blockchain.validation.AssessmentFailure.BLOCK_LAST_HASH_WRONG;
import static com.flockinger.groschn.blockchain.validation.AssessmentFailure.BLOCK_POSITION_TOO_HIGH;

import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.messaging.sync.SmartBlockSynchronizer;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.validation.impl.BlockValidator;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.AbstractMessageListener;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FreshBlockListener extends AbstractMessageListener<Block> {

  @Autowired
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("blockValidator")
  private BlockValidator validator;
  @Autowired
  private BlockMaker blockMaker;
  @Autowired
  private SmartBlockSynchronizer smartSynchronizer;
  
  @Autowired
  @Qualifier("BlockId_Cache")
  private Cache<String, String> blockIdCache;
  
  private final static Logger LOG = LoggerFactory.getLogger(FreshBlockListener.class);
  
  @Override
  protected void handleMessage(Block block) {
    try {
      var assessMent = validator.validate(block);
      if(assessMent.isValid()) {
        blockMaker.generation(STOP);
        blockService.saveUnchecked(block);
        blockMaker.generation(RESTART);
      } else {
        throw new AssessmentFailedException(assessMent.getReasonOfFailure(), 
            assessMent.getFailure());
      }
    } catch (AssessmentFailedException e) {
      LOG.error("Invalid Block-Message received maybe recoverable: " + e.getMessage(), e);
      // if the validation failed due to an outdated chain or or some faulty blocks in the chain
      // try to Resynchronize the Blockchain.
      if(BLOCK_LAST_HASH_WRONG.equals(e.getFailure()) || BLOCK_POSITION_TOO_HIGH.equals(e.getFailure())) {
        var startPosition = Math.max(2L, block.getPosition());//TODO maybe do full sync here!!
        smartSynchronizer.sync(startPosition);
      }
    } catch (RuntimeException e) {
      LOG.error("Something unexpected happened while storing fresh block!", e);
    }
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
