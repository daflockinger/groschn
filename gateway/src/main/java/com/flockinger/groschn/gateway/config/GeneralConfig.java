package com.flockinger.groschn.gateway.config;

import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;
import com.flockinger.groschn.messaging.inbound.NoOpMessageListener;

@Configuration
public class GeneralConfig {
  
  public final static String DEFAULT_PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
  
  @Value("${blockchain.messaging.thread-pool.size}")
  private Integer threadPoolSize;
  
  @Bean
  public Provider getDefaultProvider() {
    Provider bouncyCastle = new BouncyCastleProvider();
    Security.addProvider(bouncyCastle);
    return bouncyCastle;
  }
  
  @Bean
  public Executor executor() {
    Executor executorService = Executors.newFixedThreadPool(threadPoolSize);
    return executorService;
  }
  
  @Bean
  public BlockSerializer serializer() {    
    return new FstSerializer(new ArrayList<Class<?>>());
  }
  
  @Bean
  public NoOpMessageListener noOpListener() {
    return new NoOpMessageListener();
  }
}
