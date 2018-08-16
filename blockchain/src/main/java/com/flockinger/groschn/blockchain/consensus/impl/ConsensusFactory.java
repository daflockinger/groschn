package com.flockinger.groschn.blockchain.consensus.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.exception.ReachingConsentFailedException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.BlockProcessRepository;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.BlockProcess;
import com.flockinger.groschn.blockchain.repository.model.ProcessStatus;
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
  @Autowired
  private BlockProcessRepository processRepo;
  
  private final static Logger LOG = LoggerFactory.getLogger(ConsensusFactory.class);
  
  @Override
  public Block reachConsensus(List<Transaction> transactions) {
    Block freshBlock = new Block(); 
    boolean isProofOfMajorityAllowed = isProofOfMajorityAllowed();
    
    if(isProofOfMajorityAllowed) {
      markStart(ConsensusType.PROOF_OF_MAJORITY);
      freshBlock = tryProofOfMajority(transactions);
    } 
    if(!isProofOfMajorityAllowed || freshBlock == null) {
      markStart(ConsensusType.PROOF_OF_WORK);
      freshBlock = proofOfWorkAlgorithm.reachConsensus(transactions);
    }
    markStop();
    return freshBlock;
  }
  
  private boolean isProofOfMajorityAllowed() {
    boolean areEnoughNodesActive = statistics.currentActiveVoterCount() >= MIN_ACTIVE_NODE_COUNT;
    boolean wereEnoughProofOfWorkBlocksMined = blockchainRepo.count() > MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM;
    
    return areEnoughNodesActive && wereEnoughProofOfWorkBlocksMined;
  }
  
  private Block tryProofOfMajority(List<Transaction> transactions) {
    Block freshBlock = null;
    try {
      freshBlock = proofOfMajorityAlgorithm.reachConsensus(transactions);
    } catch (ReachingConsentFailedException e) {
      markFailed();
      LOG.warn("Proof of Majority Algorithm failed!", e);
    }
    return freshBlock;
  }

  private void markStart(ConsensusType type) {
    BlockProcess process = new BlockProcess();
    process.setStartedAt(new Date());
    process.setStatus(ProcessStatus.RUNNING);
    process.setConsensus(type);
    processRepo.save(process);
  }
  
  private void markStop() {
    BlockProcess process = processRepo.findFirstByOrderByStartedAtDesc().get();
    process.setFinishedAt(new Date());
    process.setStatus(ProcessStatus.DONE);
    processRepo.save(process);
  }
  
  private void markFailed() {
    BlockProcess process = processRepo.findFirstByOrderByStartedAtDesc().get();
    process.setStatus(ProcessStatus.FAILED);
    processRepo.save(process);
  }
  
  @Override
  public void stopFindingConsensus() {
    Optional<BlockProcess> process = processRepo.findFirstByOrderByStartedAtDesc();
    if(process.isPresent() && !process.get().getStatus().equals(ProcessStatus.DONE)) {
      process.get().setStatus(ProcessStatus.STOPPED);
      processRepo.save(process.get());
    }
    proofOfWorkAlgorithm.stopFindingConsensus();
    proofOfMajorityAlgorithm.stopFindingConsensus();
  }
  
  @Override
  public boolean isProcessing() {
    return proofOfWorkAlgorithm.isProcessing() || proofOfMajorityAlgorithm.isProcessing();
  }
  
  public Optional<Date> lastProcessStartDate() {
    Optional<BlockProcess> lastProcess = processRepo.findFirstByOrderByStartedAtDesc();
    if(lastProcess.isPresent()) {
      return Optional.ofNullable(lastProcess.get().getStartedAt());
    } else {
      return Optional.empty();
    }
  }
}
