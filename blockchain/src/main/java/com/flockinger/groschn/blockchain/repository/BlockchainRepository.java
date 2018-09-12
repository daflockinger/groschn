package com.flockinger.groschn.blockchain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;

public interface BlockchainRepository extends MongoRepository<StoredBlock, String>{
  
  Optional<StoredBlock> findByPosition(Long position);
  Optional<StoredBlock> findByHash(String hash);
  
  List<StoredBlock> findTop3ByConsentTypeOrderByPositionDesc(ConsensusType type);
  
  Optional<StoredBlock> findFirstByOrderByPositionDesc();
  
  Optional<StoredBlock> findByTransactionsTransactionHash(String transactionHash);
  
  Optional<StoredBlock> findFirstByTransactionsOutputsPublicKeyOrderByPositionDesc(String publicKey);
  
  Optional<StoredBlock> findFirstByTransactionsInputsPublicKeyOrderByPositionDesc(String publicKey);
  
  List<StoredBlock> findByPositionGreaterThanEqualAndTransactionsOutputsPublicKey(Long position, String publicKey);
  
  List<StoredBlock> findByPositionBetween(Long startingPosition, Long endPosition);
}
