package com.flockinger.groschn.blockchain.transaction;

import java.util.List;
import java.util.Optional;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.model.TransactionPointCut;

public interface TransactionManager {

  List<Transaction> fetchTransactionsFromPool(long maxByteSize);
  
  /**
   * Fetches output transaction from within the blockchain by pointcut parameters.
   * 
   * @param pointCut
   * @return
   */
  Optional<TransactionOutput> findTransactionFromPointCut(TransactionPointCut pointCut);
  
  
  Transaction createSignedTransaction(TransactionDto transactionSigningRequest); 
}
