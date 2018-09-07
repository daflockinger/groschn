package com.flockinger.groschn.blockchain.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredWallet;

public interface WalletRepository extends MongoRepository<StoredWallet, String> {
  Optional<StoredWallet> findByPublicKey(String publicKey);
}
