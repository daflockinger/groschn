package com.flockinger.groschn.blockchain.validation;

import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.model.Block;

public interface ConsentValidator {
  Assessment validate(Block block, Block lastBlock);
  ConsensusType type();
}
