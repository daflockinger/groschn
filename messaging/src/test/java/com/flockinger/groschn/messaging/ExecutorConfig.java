package com.flockinger.groschn.messaging;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;

@Configuration
public class ExecutorConfig {

  @Bean
  public Executor executor() {
    Executor executorService = Executors.newFixedThreadPool(23);
    return executorService;
  }
  
  @Bean
  public BlockSerializer serializer() {
    return new FstSerializer(new ArrayList<>());
  }
}
