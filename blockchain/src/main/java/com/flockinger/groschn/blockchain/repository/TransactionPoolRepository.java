package com.flockinger.groschn.blockchain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;

public interface TransactionPoolRepository extends MongoRepository<StoredPoolTransaction, String>{
  Iterable<StoredPoolTransaction> findByStatusOrderByCreatedAtAsc(TransactionStatus status);
    
  boolean existsByTransactionHash(String transactionHash);
  
  Page<StoredPoolTransaction> findByStatusOrderByCreatedAtAsc(TransactionStatus status, Pageable page);
}
