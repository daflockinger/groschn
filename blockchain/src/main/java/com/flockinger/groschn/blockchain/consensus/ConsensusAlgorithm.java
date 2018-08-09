package com.flockinger.groschn.blockchain.consensus;

import java.util.List;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;

public interface ConsensusAlgorithm {
  Block reachConsensus(List<Transaction> transactions);
}
