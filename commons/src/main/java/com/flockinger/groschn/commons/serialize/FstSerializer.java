package com.flockinger.groschn.commons.serialize;

import java.util.List;
import org.apache.commons.collections4.ListUtils;
import org.nustaq.serialization.FSTConfiguration;
import com.flockinger.groschn.commons.exception.SerializationException;
import com.flockinger.groschn.commons.serialize.BlockSerializer;


public class FstSerializer implements BlockSerializer {

  /**
   * Must be static and initiated only once to be fast, and it's thread-safe anyways.
   */
  private final static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
  
  public FstSerializer(List<Class<?>> classesToRegister) {
    ListUtils.emptyIfNull(classesToRegister)
      .forEach(conf::registerClass);
  }
  
  @Override
  public byte[] serialize(Object entity) {
    byte[] serializedEntity = new byte[0];
    try {
      serializedEntity = conf.asByteArray(entity);
    } catch (Exception e) {
      throw new SerializationException("Entity cannot be serialized!", e);
    }
    return serializedEntity;
  }

  @Override
  public <T> T deserialize(byte[] serializedEntity, Class<T> type) {
    Object deserializedEntity = null;
    try {
      deserializedEntity = conf.asObject(serializedEntity);
    } catch (Exception e) {
      throw new SerializationException("Entity cannot be deserialized!", e);
    }
    if(!type.isInstance(deserializedEntity)) {
      throw new SerializationException("Deserialized entity is not of type " + type.getSimpleName());
    }
    return type.cast(deserializedEntity);
  }
}
