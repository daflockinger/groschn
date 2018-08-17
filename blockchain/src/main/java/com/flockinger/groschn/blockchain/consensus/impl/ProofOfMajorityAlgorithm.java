package com.flockinger.groschn.blockchain.consensus.impl;

import java.util.List;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;

@Component("ProofOfMajority")
public class ProofOfMajorityAlgorithm implements ConsensusAlgorithm {
  
  /**
   * Minimum amount of nodes to have a secure PoM consensus.
   */
  public final static Long MIN_ACTIVE_NODE_COUNT = 100l;
  
  private final static Long SEVEN_DAYS_IN_MILLISECONDS = 7l * 24l * 60l * 60l * 1000l;
  /**
   * Minimum amount of PoW blocks needed to be mined before switch to PoM.
   */
  public final static Long MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM = SEVEN_DAYS_IN_MILLISECONDS / ProofOfWorkAlgorithm.MINING_RATE_MILLISECONDS;
  
  
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
