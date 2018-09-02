package com.flockinger.groschn.blockchain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;

public interface TransactionPoolRepository extends MongoRepository<StoredPoolTransaction, String>{
  Iterable<StoredPoolTransaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
  boolean existsByTransactionId(String transactionId);
}
