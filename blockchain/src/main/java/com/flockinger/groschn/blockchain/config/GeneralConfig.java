package com.flockinger.groschn.blockchain.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableRetry
@EnableAsync
@EnableMongoRepositories(basePackages="com.flockinger.groschn.blockchain.repository")
public class GeneralConfig {

  @Bean
  public ModelMapper mapper() {
    return new ModelMapper();
  }
  
  @Bean
  public TaskScheduler taskScheduler() {
      return new ThreadPoolTaskScheduler();
  }
}
