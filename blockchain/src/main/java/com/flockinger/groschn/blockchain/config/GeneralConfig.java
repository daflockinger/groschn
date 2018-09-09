package com.flockinger.groschn.blockchain.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

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
  public ExecutorService executor() {
    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
    return executorService;
  }
  
}
