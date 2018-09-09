package com.flockinger.groschn.blockchain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class TestConfig {
  
  @Value("${blockchain.messaging.thread-pool.size}")
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
}
