package com.flockinger.groschn.blockchain;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;

public class TestConfig {
  @Bean
  public ModelMapper mapper() {
    return new ModelMapper();
  }
}
