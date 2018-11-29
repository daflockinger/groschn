package com.flockinger.groschn.commons.config;

import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.crypto.KeyAESCipher;
import com.flockinger.groschn.commons.crypto.KeyCipher;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.hash.MerkleRootCalculator;
import com.flockinger.groschn.commons.hash.MultiHashGenerator;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.sign.EcdsaSecpSigner;
import com.flockinger.groschn.commons.sign.Signer;
import java.security.Provider;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
  public Compressor compressor(BlockSerializer serializer) {
    return new Compressor(serializer);
  }

  @Bean
  public KeyCipher cipher(Provider provider) {
    return new KeyAESCipher(provider);
  }

  @Bean
  public HashGenerator hasher(Provider provider) {
    return new MultiHashGenerator(new MerkleRootCalculator());
  }

  @Bean
  public Signer ecdsaSecpSigner(Provider provider) {
    return new EcdsaSecpSigner(provider);
  }
}
