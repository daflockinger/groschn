package com.flockinger.groschn.blockchain.validation;

import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;

public interface ConsentValidator extends Validator<Consent> {

  ConsensusType type();
}
