package com.flockinger.groschn.blockchain.config;

import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfig {

  public final static String DEFAULT_PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
  
  @Bean
  public Provider getDefaultProvider() {
    Provider bouncyCastle = new BouncyCastleProvider();
    Security.addProvider(bouncyCastle);
    return bouncyCastle;
  }
}
