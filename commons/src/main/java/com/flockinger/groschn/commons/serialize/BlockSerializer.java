package com.flockinger.groschn.commons.serialize;

public interface BlockSerializer {

  byte[] serialize(Object entity);
  
  <T> T deserialize(byte[] serializedEntity, Class<T> type);
}
