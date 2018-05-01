package com.flockinger.groschn.blockchain.transaction;

import java.util.List;
import com.flockinger.groschn.blockchain.exception.TransactionAlreadyClearedException;
import com.flockinger.groschn.blockchain.model.Transaction;

public interface TransactionManager {

  List<Transaction> fetchTransactionsFromPool(int maxAmount);
  
  void clearTransactions(List<String> transactionIds) throws TransactionAlreadyClearedException;
}
