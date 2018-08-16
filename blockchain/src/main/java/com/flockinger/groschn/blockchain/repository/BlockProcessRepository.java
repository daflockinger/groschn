package com.flockinger.groschn.blockchain.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.flockinger.groschn.blockchain.repository.model.BlockProcess;

public interface BlockProcessRepository extends MongoRepository<BlockProcess, String> {

  Optional<BlockProcess> findFirstByOrderByStartedAtDesc();
}
