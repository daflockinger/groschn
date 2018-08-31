package com.flockinger.groschn.blockchain.blockworks;

import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Hashable;

public interface HashGenerator {
  
  String generateHash(Hashable hashable) throws HashingException;
  
  boolean isHashCorrect(String hash, Hashable hashable);
}
