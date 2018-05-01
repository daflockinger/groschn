package com.flockinger.groschn.blockchain.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages="com.flockinger.groschn.blockchain.repository")
public class GeneralConfig {

  @Bean
  public ModelMapper mapper() {
    return new ModelMapper();
  }
}
