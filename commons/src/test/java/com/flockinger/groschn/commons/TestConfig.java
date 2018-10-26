package com.flockinger.groschn.commons;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.flockinger.groschn.commons.model.TestBlock;
import com.flockinger.groschn.commons.model.TestConsensusType;
import com.flockinger.groschn.commons.model.TestConsent;
import com.flockinger.groschn.commons.model.TestTransaction;
import com.flockinger.groschn.commons.model.TestTransactionInput;
import com.flockinger.groschn.commons.model.TestTransactionOutput;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;

public class TestConfig {
  
  @Bean
  public BlockSerializer serializer() {
    var registered = new ArrayList<Class<?>>();
    registered.add(TestBlock.class);
    registered.add(TestTransaction.class);
    registered.add(TestTransactionInput.class);
    registered.add(TestTransactionOutput.class);
    registered.add(TestConsent.class);
    registered.add(TestConsensusType.class); 
    
    return new FstSerializer(registered);
  }
}
