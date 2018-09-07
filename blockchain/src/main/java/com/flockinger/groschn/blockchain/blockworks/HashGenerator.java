package com.flockinger.groschn.blockchain.blockworks;

import java.util.List;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.model.Sequential;

public interface HashGenerator {
  
  String generateHash(Hashable hashable) throws HashingException;
  
  <T extends Sequential> byte[] generateListHash(List<T> sortable) throws HashingException;
  
  boolean isHashCorrect(String hash, Hashable hashable);
}
