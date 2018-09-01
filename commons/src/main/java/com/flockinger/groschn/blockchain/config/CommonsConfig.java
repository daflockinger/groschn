package com.flockinger.groschn.blockchain.config;

import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.blockchain.util.compress.CompressionUtils;
import com.flockinger.groschn.blockchain.util.crypto.KeyAESCipher;
import com.flockinger.groschn.blockchain.util.crypto.KeyCipher;
import com.flockinger.groschn.blockchain.util.hash.HashGenerator;
import com.flockinger.groschn.blockchain.util.hash.MultiHashGenerator;
import com.flockinger.groschn.blockchain.util.serialize.BlockSerializer;
import com.flockinger.groschn.blockchain.util.serialize.FstSerializer;
import com.flockinger.groschn.blockchain.util.sign.EcdsaSecpSigner;
import com.flockinger.groschn.blockchain.util.sign.Signer;

@Configuration
@ConditionalOnBean(BlockSerializer.class)
public class CommonsConfig {
  
  public final static String DEFAULT_PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;
  
  @Bean
  public Provider getDefaultProvider() {
    Provider bouncyCastle = new BouncyCastleProvider();
    Security.addProvider(bouncyCastle);
    return bouncyCastle;
  }
  
  @Bean
  @ConditionalOnBean(HashGenerator.class)
  public MerkleRootCalculator merkleRootCalculator() {
    return new MerkleRootCalculator();
  }
  
  @Bean
  @ConditionalOnBean(BlockSerializer.class)
  public CompressionUtils compressor() {
    return new CompressionUtils();
  }
  
  @Bean
  public KeyCipher cipher() {
    return new KeyAESCipher(getDefaultProvider());
  }
  
  @Bean
  @ConditionalOnBean(BlockSerializer.class)
  public HashGenerator hasher() {
    return new MultiHashGenerator(getDefaultProvider());
  }
  
  @Bean
  public Signer ecdsaSecpSigner() {
    return new EcdsaSecpSigner(getDefaultProvider());
  }
}
