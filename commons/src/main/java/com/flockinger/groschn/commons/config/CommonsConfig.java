package com.flockinger.groschn.commons.config;

import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.flockinger.groschn.commons.MerkleRootCalculator;
import com.flockinger.groschn.commons.compress.CompressionUtils;
import com.flockinger.groschn.commons.crypto.KeyAESCipher;
import com.flockinger.groschn.commons.crypto.KeyCipher;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.hash.MultiHashGenerator;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;
import com.flockinger.groschn.commons.sign.EcdsaSecpSigner;
import com.flockinger.groschn.commons.sign.Signer;

@Configuration
public class CommonsConfig {
  
  public final static String DEFAULT_PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
  
  @Bean
  public Provider getDefaultProvider() {
    Provider bouncyCastle = new BouncyCastleProvider();
    Security.addProvider(bouncyCastle);
    return bouncyCastle;
  }
  
  @Bean
  public MerkleRootCalculator merkleRootCalculator() {
    return new MerkleRootCalculator();
  }
  
  @Bean
  public CompressionUtils compressor() {
    return new CompressionUtils();
  }
  
  @Bean
  public KeyCipher cipher() {
    return new KeyAESCipher(getDefaultProvider());
  }
  
  @Bean
  public HashGenerator hasher() {
    return new MultiHashGenerator(getDefaultProvider());
  }
  
  @Bean
  public Signer ecdsaSecpSigner() {
    return new EcdsaSecpSigner(getDefaultProvider());
  }
}
