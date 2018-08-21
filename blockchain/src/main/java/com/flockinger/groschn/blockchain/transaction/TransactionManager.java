package com.flockinger.groschn.blockchain.transaction;

import java.util.List;
import java.util.Optional;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.model.TransactionPointCut;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;

public interface TransactionManager {

  List<Transaction> fetchTransactionsFromPool(long maxByteSize);
  
  /**
   * Fetches output transaction from within the blockchain by pointcut parameters.
   * 
   * @param pointCut
   * @return
   */
  Optional<TransactionOutput> findTransactionFromPointCut(TransactionPointCut pointCut);
}
