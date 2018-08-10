package com.flockinger.groschn.blockchain.consensus.impl;

import java.util.List;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.PowConsent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;

@Component("ProofOfMajority")
public class ProofOfMajorityAlgorithm implements ConsensusAlgorithm {

  
  
  
  //TODO implement!!!
  
  @Override
  public Block reachConsensus(List<Transaction> transactions) {
    return null;
  }

  @Override
  public void stopFindingConsensus() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isProcessing() {
    // TODO Auto-generated method stub
    return false;
  }

}
