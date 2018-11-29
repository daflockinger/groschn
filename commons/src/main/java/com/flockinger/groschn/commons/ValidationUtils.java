package com.flockinger.groschn.commons;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.model.Sequential;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.exception.HashingException;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;
import java.util.List;

public class ValidationUtils {

  private final HashGenerator hasher;
  private final Signer signer;
  private final Compressor compressor;

  public ValidationUtils(HashGenerator hasher, Signer signer,
      Compressor compressor) {
    this.hasher = hasher;
    this.signer = signer;
    this.compressor = compressor;
  }

  public <T extends Sequential> byte[] generateListHash(List<T> sortable) throws HashingException {
    return hasher.generateListHash(sortable);
  }

  public boolean isHashCorrect(String hash, Hashable<?> hashable) {
    return hasher.isHashCorrect(hash, hashable);
  }

  public <T extends Hashable<T>> String calculateMerkleRootHash(List<T> entities) {
    return hasher.calculateMerkleRootHash(entities);
  }

  public boolean isSignatureValid(byte[] transactionHash, String publicKey, String signature) {
    return signer.isSignatureValid(transactionHash, publicKey, signature);
  }

  public <T extends Hashable<T>> int compressedByteSize(List<T> entities) {
    return compressor.compressedByteSize(entities);
  }
}
