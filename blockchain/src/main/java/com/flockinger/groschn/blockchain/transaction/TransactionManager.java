package com.flockinger.groschn.blockchain.transaction;

import java.util.List;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;

public interface TransactionManager {

  List<Transaction> fetchTransactionsFromPool(long maxByteSize);
  
  Transaction createSignedTransaction(TransactionDto transactionSigningRequest); 
  
  void storeTransaction(Transaction transaction);
  
  void updateTransactionStatuses(List<Transaction> transactions, TransactionStatus status);
}
