package com.flockinger.groschn.blockchain.blockworks;

import java.util.List;
import com.flockinger.groschn.blockchain.model.Transaction;

public interface RewardGenerator {
  List<Transaction> generateRewardTransaction(List<Transaction> blockTransactions);
}
