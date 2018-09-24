package com.flockinger.groschn.blockchain.util.serialize;

public interface BlockSerializer {

  byte[] serialize(Object entity);
  
  <T> T deserialize(byte[] serializedEntity, Class<T> type);
}
