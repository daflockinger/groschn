package com.flockinger.groschn.blockchain.consensus.model;

import com.flockinger.groschn.blockchain.model.Hashable;

public interface Consent extends Hashable {

	
	ConsensusType getType();
}
