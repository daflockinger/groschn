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
import com.flockinger.groschn.blockchain.validation.Validator;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageListener;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class FreshBlockListener implements  MessageListener<MessagePayload> {

  @Autowired
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("lastBlockValidator")
  private Validator<Block> validator;
  @Autowired
  private BlockMaker blockMaker;
  @Autowired
  private SmartBlockSynchronizer smartSynchronizer;
  
  @Autowired
  @Qualifier("BlockId_Cache")
  private Cache<String, String> blockIdCache;

  @Autowired
  private MessagePackageHelper helper;
  
  private final static Logger LOG = LoggerFactory.getLogger(FreshBlockListener.class);

  @Override
  public void receiveMessage(Message<MessagePayload> message) {
    var receivedBlock = helper.verifyAndUnpackMessage(message, blockIdCache, Block.class);

    receivedBlock.ifPresent(this::handleMessage);
  }

  private synchronized void handleMessage(Block block) {
    try {
      var assessment = validator.validate(block);
      if(assessment.isValid()) {
        blockMaker.generation(STOP);
        blockService.saveUnchecked(block);
        blockMaker.generation(RESTART);
      } else {
        throw new AssessmentFailedException(assessment.getReasonOfFailure(),
            assessment.getFailure());
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
}
