package com.flockinger.groschn.blockchain.transaction;

import java.util.List;
import com.flockinger.groschn.blockchain.api.dto.TransactionIdDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionStatusDto;
import com.flockinger.groschn.blockchain.api.dto.ViewTransactionDto;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.exception.TransactionNotFoundException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;

public interface TransactionManager {

  List<Transaction> fetchTransactionsBySize(long maxByteSize);
  
  List<Transaction> fetchTransactionsPaginated(int page, int size);
  
  Transaction createSignedTransaction(TransactionDto transactionSigningRequest); 
  
  TransactionIdDto storeTransaction(Transaction transaction);
  
  void updateTransactionStatuses(List<Transaction> transactions, TransactionStatus status);
  
  List<ViewTransactionDto> getTransactionsFromPublicKey(String publicKey);
  
  TransactionStatusDto getStatusOfTransaction(String transactionHash) throws TransactionNotFoundException;
}
