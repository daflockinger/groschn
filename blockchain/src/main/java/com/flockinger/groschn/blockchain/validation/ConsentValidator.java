package com.flockinger.groschn.blockchain.validation;

import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.model.Block;

public interface ConsentValidator extends Validator<Block> {

  ConsensusType type();
}
