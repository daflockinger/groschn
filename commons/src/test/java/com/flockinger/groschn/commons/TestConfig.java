package com.flockinger.groschn.commons;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.flockinger.groschn.commons.model.Block;
import com.flockinger.groschn.commons.model.ConsensusType;
import com.flockinger.groschn.commons.model.Consent;
import com.flockinger.groschn.commons.model.Transaction;
import com.flockinger.groschn.commons.model.TransactionInput;
import com.flockinger.groschn.commons.model.TransactionOutput;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;

public class TestConfig {
  
  @Bean
  public BlockSerializer serializer() {
    var registered = new ArrayList<Class<?>>();
    registered.add(Block.class);
    registered.add(Transaction.class);
    registered.add(TransactionInput.class);
    registered.add(TransactionOutput.class);
    registered.add(Consent.class);
    registered.add(ConsensusType.class); 
    
    return new FstSerializer(registered);
  }
}
