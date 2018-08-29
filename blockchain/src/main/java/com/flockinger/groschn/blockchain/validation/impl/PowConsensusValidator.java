package com.flockinger.groschn.blockchain.validation.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.exception.validation.ValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.ConsentValidator;

@Component("ProofOfWork_Validator")
public class PowConsensusValidator implements ConsentValidator {

  
  @Autowired
  private BlockStorageService blockService;;
  
  
  /*
   Things to verify:
   1. was difficulty applied correctly -> enough zeros on hash
   2. was difficulty adjusted correctly from last time
   3. is timestamp not in the future
   ... maybe think of something else ?!
   * */
  
  @Override
  public Assessment validate(Block value) {
    Assessment isConsensusCorrect = new Assessment();
    Consent consent = value.getConsent();
    
    try {
      Block lastBlock = null; //TODO fetch last POW block extract functionality from 
                              // wherever it's used already to the blockService!!
      
      // 2. was difficulty adjusted correctly from last time
      wasLastDifficultyAdjustmentCorrect(consent, lastBlock.getConsent());
      // 3. is timestamp not in the future
      isTimestampNotInTheFuture(consent);
      
      isConsensusCorrect.setValid(true);
    } catch (ValidationException e) {
      isConsensusCorrect.setValid(false);
      isConsensusCorrect.setReasonOfFailure(e.getMessage());
    }
    return isConsensusCorrect;
  }
  
  private void wasLastDifficultyAdjustmentCorrect(Consent consent, Consent lastConsent) {
    
    
    
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
