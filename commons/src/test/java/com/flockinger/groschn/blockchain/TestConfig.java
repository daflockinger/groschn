package com.flockinger.groschn.blockchain;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.ConsensusType;
import com.flockinger.groschn.blockchain.model.Consent;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.util.serialize.BlockSerializer;
import com.flockinger.groschn.blockchain.util.serialize.FstSerializer;

public class TestConfig {
  
  @Value("${blockchain.messaging.thread-pool.size:20}")
  private Integer threadPoolSize;
  
  @Bean
  public ExecutorService executor() {
    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
    return executorService;
  }
  
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
