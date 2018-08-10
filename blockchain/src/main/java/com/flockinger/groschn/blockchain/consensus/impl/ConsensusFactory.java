package com.flockinger.groschn.blockchain.consensus.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.messaging.members.ElectionStatistics;
import static com.flockinger.groschn.blockchain.consensus.model.ProofOfMajorityConsent.*;

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
  @Autowired
  private BlockchainRepository blockchainRepo;
  
  @Override
  public Block reachConsensus(List<Transaction> transactions) {
    Block freshBlock = new Block(); 
    if(canProofOfMajorityBeUsed()) {
      freshBlock = proofOfMajorityAlgorithm.reachConsensus(transactions);
    } else {
      freshBlock = proofOfWorkAlgorithm.reachConsensus(transactions);
    }
    return freshBlock;
  }
  
  private boolean canProofOfMajorityBeUsed() {
    boolean areEnoughNodesActive = statistics.currentActiveVoterCount() >= MIN_ACTIVE_NODE_COUNT;
    boolean wereEnoughProofOfWorkBlocksMined = blockchainRepo.count() > MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM;
    
    return areEnoughNodesActive && wereEnoughProofOfWorkBlocksMined;
  }

  @Override
  public void stopFindingConsensus() {}
  @Override
  public boolean isProcessing() {
    return false;
  }
}
