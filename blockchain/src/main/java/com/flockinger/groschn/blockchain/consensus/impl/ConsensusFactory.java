package com.flockinger.groschn.blockchain.consensus.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.messaging.members.ElectionStatistics;

@Component("ConsensusDecider")
public class ConsensusFactory implements ConsensusAlgorithm {
  
  @Qualifier("POW")
  @Autowired
  private ConsensusAlgorithm proofOfWorkAlgorithm;
  
  @Qualifier("ProofOfMajority")
  @Autowired
  private ConsensusAlgorithm proofOfMajorityAlgorithm;
  
  @Autowired
  private ElectionStatistics statistics;
  
  @Override
  public Block reachConsensus(List<Transaction> transactions) {
    return null;
  }
  
  
  
  private boolean canProofOfMajorityBeUsed() {
    return false;
  }

}
