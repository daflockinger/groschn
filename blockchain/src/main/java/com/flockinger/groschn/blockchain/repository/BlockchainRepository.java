package com.flockinger.groschn.blockchain.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;

public interface BlockchainRepository extends MongoRepository<StoredBlock, String>{
  
  Optional<StoredBlock> findByPosition(Long position);
  Optional<StoredBlock> findByHash(String hash);
  
  //TODO add queries to fetch by transaction sender/receiver
}
