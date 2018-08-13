package com.flockinger.groschn.blockchain.transaction;

import java.util.List;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionPointCut;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.google.common.base.Optional;

public interface TransactionManager {

  List<Transaction> fetchTransactionsFromPool(long maxByteSize);
  
  //TODO check if I really need that
  void clearTransactions(List<String> transactionIds);
  
  void updateTransactionStatus(String transactionId, TransactionStatus status);
  
  /**
   * Fetches output transaction from within the blockchain by pointcut parameters.
   * 
   * @param pointCut
   * @return
   */
  Optional<Transaction> findTransactionFromPointCut(TransactionPointCut pointCut);
}
