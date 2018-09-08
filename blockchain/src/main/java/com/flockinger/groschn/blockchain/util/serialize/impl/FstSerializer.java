package com.flockinger.groschn.blockchain.util.serialize.impl;

import org.nustaq.serialization.FSTConfiguration;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.exception.SerializationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.util.serialize.BlockSerializer;

@Component
public class FstSerializer implements BlockSerializer {

  /**
   * Must be static ans instantated only once to be fast, and it's thread-safe anyways.
   */
  private final static FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
  
  public FstSerializer() {
    conf.registerClass(Block.class);
    conf.registerClass(Transaction.class);
    conf.registerClass(TransactionInput.class);
    conf.registerClass(TransactionOutput.class);
    conf.registerClass(Consent.class);
    conf.registerClass(ConsensusType.class);
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
