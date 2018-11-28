package com.flockinger.groschn.blockchain.repository;

import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface BlockchainRepository extends MongoRepository<StoredBlock, String>{
  
  Optional<StoredBlock> findByPosition(Long position);
  Optional<StoredBlock> findByHash(String hash);
  
  List<StoredBlock> findTop3ByConsentTypeOrderByPositionDesc(ConsensusType type);
  
  List<StoredBlock> findFirstByPositionLessThanAndConsentTypeOrderByPositionDesc(Long position, ConsensusType type);
  
  Optional<StoredBlock> findFirstByOrderByPositionDesc();
  
  Optional<StoredBlock> findByTransactionsTransactionHash(String transactionHash);
  
  Optional<StoredBlock> findFirstByTransactionsOutputsPublicKeyOrderByPositionDesc(String publicKey);
  
  Optional<StoredBlock> findFirstByTransactionsInputsPublicKeyOrderByPositionDesc(String publicKey);
  
  List<StoredBlock> findByPositionGreaterThanEqualAndTransactionsOutputsPublicKey(Long position, String publicKey);
  
  List<StoredBlock> findByTransactionsOutputsPublicKey(String publicKey);
  
  List<StoredBlock> findByTransactionsInputsPublicKey(String publicKey);

  @Query("{\"position\" : {\"$gte\" : ?0, \"$lte\" : ?1}}")
  List<StoredBlock> findByPositionBetweenInclusive(Long startingPosition, Long endPosition);

  @Transactional
  void removeByPosition(Long position);
}
