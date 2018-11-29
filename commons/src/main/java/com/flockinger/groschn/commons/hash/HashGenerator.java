package com.flockinger.groschn.commons.hash;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.model.Sequential;
import com.flockinger.groschn.commons.exception.HashingException;
import java.util.List;

public interface HashGenerator {
  
  String generateHash(Hashable<?> hashable) throws HashingException;
  
  <T extends Sequential> byte[] generateListHash(List<T> sortable) throws HashingException;
  
  boolean isHashCorrect(String hash, Hashable<?> hashable);

  <T extends Hashable<T>> String calculateMerkleRootHash(List<T> entities);
}
