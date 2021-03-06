package com.flockinger.groschn.blockchain;

import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.commons.BlockchainUtilsFactory;
import com.flockinger.groschn.commons.compress.Compressor;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync //FIXME maybe use that only when really needed!
public class TestConfig {
  
  @Value("${blockchain.messaging.thread-pool.size:20}")
  private Integer threadPoolSize;
  
  @Bean
  public ModelMapper mapper() {
    return new ModelMapper();
  }
  
  @Bean
  public ExecutorService executor() {
    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
    return executorService;
  }
  @Bean
  public Compressor compressor() {
    var registered = new ArrayList<Class<?>>();
    registered.add(Block.class);
    registered.add(Transaction.class);
    registered.add(TransactionInput.class);
    registered.add(TransactionOutput.class);
    registered.add(Consent.class);
    registered.add(ConsensusType.class);

    return BlockchainUtilsFactory.createCompressor(registered);
  }

}
