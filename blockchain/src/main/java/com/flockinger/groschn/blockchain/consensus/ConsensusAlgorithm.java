package com.flockinger.groschn.blockchain.consensus;

import java.util.List;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import reactor.core.publisher.Mono;

public interface ConsensusAlgorithm {
  Mono<Block> reachConsensus(List<Transaction> transactions);
  
  void stopFindingConsensus();
}
