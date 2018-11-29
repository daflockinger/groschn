package com.flockinger.groschn.commons;

import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.crypto.KeyAESCipher;
import com.flockinger.groschn.commons.crypto.KeyCipher;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.hash.MerkleRootCalculator;
import com.flockinger.groschn.commons.hash.MultiHashGenerator;
import com.flockinger.groschn.commons.serialize.FstSerializer;
import com.flockinger.groschn.commons.sign.EcdsaSecpSigner;
import com.flockinger.groschn.commons.sign.Signer;
import java.security.Provider;
import java.util.List;

public class BlockchainUtilsFactory {

  public static HashGenerator createHashGenerator(Provider provider) {
    return new MultiHashGenerator(new MerkleRootCalculator());
  }

  public static Compressor createCompressor(List<Class<?>> registeredClasses) {
    return new Compressor(new FstSerializer(registeredClasses));
  }

  public static Signer createSigner(Provider provider) {
    return  new EcdsaSecpSigner(provider);
  }

  public static KeyCipher createCipher(Provider provider) {
    return new KeyAESCipher(provider);
  }

  public static TransactionUtils buildTransactionUtils(Signer signer, HashGenerator hasher, Compressor compressor) {
    return new TransactionUtils(signer, hasher, compressor);
  }

  public static ValidationUtils buildValidationUtils(Signer signer, HashGenerator hasher, Compressor compressor) {
    return new ValidationUtils(hasher,signer,compressor);
  }
}
