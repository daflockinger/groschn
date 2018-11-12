package com.flockinger.groschn.blockchain.consensus;

import java.util.List;
import java.util.Optional;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;

public interface ConsensusAlgorithm {
  
  Optional<Block> reachConsensus(List<Transaction> transactions);
  
  void stopFindingConsensus();
}
