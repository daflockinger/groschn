package com.flockinger.groschn.blockchain.validation.impl;

import static com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm.LEADING_ZERO;
import static com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm.MINING_RATE_MILLISECONDS;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.ConsentValidator;

@Component("ProofOfWork_Validator")
public class PowConsensusValidator implements ConsentValidator {
  
  @Autowired
  private BlockStorageService blockService;
  
  @Override
  public Assessment validate(Block value) {
    Assessment isConsensusCorrect = new Assessment();
    Consent consent = value.getConsent();
    try {
      Block lastBlock = blockService.getLatestProofOfWorkBlock();
      // 1. was difficulty applied correctly -> enough zeros on hash
      wasDifficultyAppliedCorrectly(value);
      // 2. was difficulty adjusted correctly from last time
      wasLastDifficultyAdjustmentCorrect(consent, lastBlock.getConsent());
      // 3. is timestamp not in the future
      isTimestampNotInTheFuture(consent);
      
      isConsensusCorrect.setValid(true);
    } catch (BlockchainException e) {
      isConsensusCorrect.setValid(false);
      isConsensusCorrect.setReasonOfFailure(e.getMessage());
    }
    return isConsensusCorrect;
  }
  
  private void wasDifficultyAppliedCorrectly(Block block) {
    Consent consent = block.getConsent();
    if(consent.getDifficulty() < 0) {
      throw new AssessmentFailedException("Block difficulty must not be negative!");
    }
    String wantedHashPrefix = StringUtils.repeat(LEADING_ZERO, consent.getDifficulty());
    if(!StringUtils.startsWith(block.getHash(), wantedHashPrefix)) {
      throw new AssessmentFailedException("Block hash is invalid, "
          + "difficulty target was not applied correctly!");
    }
  }
  
  private void wasLastDifficultyAdjustmentCorrect(Consent consent, Consent lastConsent) {
    String errorMessage = null;
    var lastMiningRate = lastConsent.getMilliSecondsSpentMining();
    var difficultyTrend = consent.getDifficulty() - lastConsent.getDifficulty();
    
    if (Math.abs(difficultyTrend) > 1) {
      errorMessage = "Mining rate cannot increase/decrease by more than one per block-mining!";
    } else if(lastMiningRate > MINING_RATE_MILLISECONDS && difficultyTrend != -1) {
      errorMessage = "Mining difficulty didn't decrease when last mining rate was too slow!";
    } else if(lastMiningRate < MINING_RATE_MILLISECONDS && difficultyTrend != 1) {
      errorMessage = "Mining difficulty didn't increase when last mining rate was too fast!";
    } else if (difficultyTrend != 0) {
      errorMessage = "Mining rate must stay the same when mining rate is exact!";
    }
    if(errorMessage != null) {
      throw new AssessmentFailedException(errorMessage);
    }
  }
  
  private void isTimestampNotInTheFuture(Consent consent) {
    if(consent.getTimestamp() > new Date().getTime()) {
      throw new AssessmentFailedException("Consent timestamp cannot be in the future!");
    }
  }

  @Override
  public ConsensusType type() {
    return ConsensusType.PROOF_OF_WORK;
  }
}
