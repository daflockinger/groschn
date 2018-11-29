package com.flockinger.groschn.gateway.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeneralConfig {
  
  public final static String DEFAULT_PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
}
