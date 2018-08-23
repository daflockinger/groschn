package com.flockinger.groschn.blockchain.validation.impl;

import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.ConsentValidator;

@Component("ProofOfWork_Validator")
public class PowConsensusValidator implements ConsentValidator {

  @Override
  public Assessment validate(Consent value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ConsensusType type() {
    return ConsensusType.PROOF_OF_WORK;
  }

}
