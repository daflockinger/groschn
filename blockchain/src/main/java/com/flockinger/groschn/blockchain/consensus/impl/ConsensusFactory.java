package com.flockinger.groschn.blockchain.consensus.impl;

import static com.flockinger.groschn.blockchain.consensus.impl.ProofOfMajorityAlgorithm.MIN_ACTIVE_NODE_COUNT;
import static com.flockinger.groschn.blockchain.consensus.impl.ProofOfMajorityAlgorithm.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.exception.ReachingConsentFailedException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.messaging.members.NetworkStatistics;

@Component("ConsensusDecider")
public class ConsensusFactory implements ConsensusAlgorithm {

  @Qualifier("POW")
  @Autowired
  private ConsensusAlgorithm proofOfWorkAlgorithm;

  @Qualifier("ProofOfMajority")
  @Autowired
  private ConsensusAlgorithm proofOfMajorityAlgorithm;

  @Autowired
  private NetworkStatistics statistics;
  @Autowired
  private BlockchainRepository blockchainRepo;

  private final static Logger LOG = LoggerFactory.getLogger(ConsensusFactory.class);

  @Override
  public Optional<Block> reachConsensus(List<Transaction> transactions) {
    Optional<Block> block = Optional.empty();
    boolean isProofOfMajorityAllowed = isProofOfMajorityAllowed();

    if (isProofOfMajorityAllowed) {
      block = proofOfMajority(transactions);
    }
    if (!isProofOfMajorityAllowed || !block.isPresent()) {
      block = proofOfWorkAlgorithm.reachConsensus(transactions);
    }
    return block;
  }

  private Optional<Block> proofOfMajority(List<Transaction> transactions) {
    Optional<Block> block = Optional.empty();
    try {
      block = proofOfMajorityAlgorithm.reachConsensus(transactions);
    } catch (ReachingConsentFailedException conesnsusFailed) {
      LOG.error("Reaching consensus through ProofOfMajority failed!", conesnsusFailed);
    }
    return block;
  }

  private boolean isProofOfMajorityAllowed() {
    boolean areEnoughNodesActive = statistics.activeNodeCount() >= MIN_ACTIVE_NODE_COUNT;
    boolean wereEnoughProofOfWorkBlocksMined =
        blockchainRepo.count() > MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM;

    return areEnoughNodesActive && wereEnoughProofOfWorkBlocksMined;
  }

  @Override
  public void stopFindingConsensus() {
    proofOfWorkAlgorithm.stopFindingConsensus();
    proofOfMajorityAlgorithm.stopFindingConsensus();
  }
}
