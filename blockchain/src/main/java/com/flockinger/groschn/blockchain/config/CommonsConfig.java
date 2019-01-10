package com.flockinger.groschn.blockchain.config;

import com.flockinger.groschn.commons.BlockchainUtilsFactory;
import com.flockinger.groschn.commons.TransactionUtils;
import com.flockinger.groschn.commons.ValidationUtils;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.crypto.KeyCipher;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;
import java.security.Provider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonsConfig {

  @Bean
  public KeyCipher cipher(Provider provider) {
    return BlockchainUtilsFactory.createCipher(provider);
  }

  @Bean
  public HashGenerator hasher(Provider provider) {
    return BlockchainUtilsFactory.createHashGenerator(provider);
  }

  @Bean
  public Signer ecdsaSecpSigner(Provider provider) {
    return BlockchainUtilsFactory.createSigner(provider);
  }

  @Bean
  public TransactionUtils transactionUtils(Signer signer, HashGenerator hasher, Compressor compressor) {
    return BlockchainUtilsFactory.buildTransactionUtils(signer, hasher, compressor);
  }

  @Bean
  public ValidationUtils validationUtils(Signer signer, HashGenerator hasher, Compressor compressor) {
    return BlockchainUtilsFactory.buildValidationUtils(signer, hasher, compressor);
  }
}
