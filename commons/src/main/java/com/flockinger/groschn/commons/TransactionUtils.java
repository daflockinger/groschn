package com.flockinger.groschn.commons;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.exception.HashingException;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;
import java.util.List;

public class TransactionUtils {
  private final Signer signer;
  private final HashGenerator hasher;
  private final Compressor compressor;

  public TransactionUtils(Signer signer, HashGenerator hasher, Compressor compressor) {
    this.signer = signer;
    this.hasher = hasher;
    this.compressor = compressor;
  }

  public String generateHash(Hashable<?> hashable) throws HashingException {
    return hasher.generateHash(hashable);
  }

  public <T extends Hashable<T>> int compressedByteSize(List<T> entities) {
    return compressor.compressedByteSize(entities);
  }

  public String sign(byte[] transactionHash, byte[] privateKey) {
    return signer.sign(transactionHash, privateKey);
  }
}
