package com.flockinger.groschn.commons;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;
import java.util.ArrayList;
import org.junit.Test;

public class TransactionUtilsTest {

  private final Signer signer = mock(Signer.class);
  private final HashGenerator hasher = mock(HashGenerator.class);
  private final Compressor compressor = mock(Compressor.class);

  private TransactionUtils utils = new TransactionUtils(signer,hasher,compressor);

  @Test
  public void generateHash() {
    var hashable = mock(Hashable.class);

    utils.generateHash(hashable);

    verify(hasher).generateHash(eq(hashable));
  }

  @Test
  public void compressedByteSize() {
    var entities = new ArrayList<Hashable>();

    utils.compressedByteSize(entities);

    verify(compressor).compressedByteSize(eq(entities));
  }

  @Test
  public void sign() {
    var sigBytes = new byte[3];
    var secretKeyBytes = new byte[45];

    utils.sign(sigBytes,secretKeyBytes);

    verify(signer).sign(eq(sigBytes), eq(secretKeyBytes));
  }
}