package com.flockinger.groschn.blockchain.consensus;

import java.util.List;

import com.flockinger.groschn.blockchain.model.Transaction;

public interface ConsensusAlgorithm {
  Agreement reachConsensus(List<Transaction> transactions);
}
