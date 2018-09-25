package com.flockinger.groschn.blockchain.config;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;

@Configuration
@EnableScheduling
@EnableMongoRepositories(basePackages="com.flockinger.groschn.blockchain.repository")
public class GeneralConfig {

  @Value("${blockchain.messaging.thread-pool.size}")
  private Integer threadPoolSize;
  
  
  @Bean
  public ModelMapper mapper() {
    return new ModelMapper();
  }
  
  @Bean
  public Executor executor() {
    Executor executorService = Executors.newFixedThreadPool(threadPoolSize);
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
