package com.flockinger.groschn.gateway.config;

import com.flockinger.groschn.commons.BlockchainUtilsFactory;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.messaging.config.MessagingProtocolConfiguration;
import com.flockinger.groschn.messaging.inbound.NoOpMessageListener;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayMessagingProtocolConfiguration extends MessagingProtocolConfiguration {

  @Value("${blockchain.messaging.thread-pool.size}")
  private Integer threadPoolSize;

  @Override
  protected Compressor messageCompressor() {
    return BlockchainUtilsFactory.createCompressor(new ArrayList<>());
  }

  @Override
  protected Executor messageExecutor() {
    Executor executorService = Executors.newFixedThreadPool(threadPoolSize);
    return executorService;
  }

  @Bean
  public NoOpMessageListener noOpListener() {
    return new NoOpMessageListener();
  }
}
