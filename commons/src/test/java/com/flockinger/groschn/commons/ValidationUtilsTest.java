package com.flockinger.groschn.commons;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.model.Sequential;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;
import java.util.ArrayList;
import org.junit.Test;

public class ValidationUtilsTest {

  private final HashGenerator hasher = mock(HashGenerator.class);
  private final Signer signer = mock(Signer.class);
  private final Compressor compressor = mock(Compressor.class);

  private ValidationUtils utils =new ValidationUtils(hasher,signer,compressor);

  @Test
  public void generateListHash() {
    var someList = new ArrayList<Sequential>();

    utils.generateListHash(someList);

    verify(hasher).generateListHash(eq(someList));
  }

  @Test
  public void isHashCorrect() {
    var hashable = mock(Hashable.class);
    utils.isHashCorrect("hash", hashable);

    verify(hasher).isHashCorrect(eq("hash"),eq(hashable));
  }

  @Test
  public void calculateMerkleRootHash() {
    var someList = new ArrayList<Hashable>();

    utils.calculateMerkleRootHash(someList);

    verify(hasher).calculateMerkleRootHash(someList);
  }

  @Test
  public void isSignatureValid() {
    var sigBytes = new byte[10];

    utils.isSignatureValid(sigBytes,"pubKey","sigsig");

    verify(signer).isSignatureValid(eq(sigBytes),eq("pubKey"),eq("sigsig"));
  }

  @Test
  public void compressedByteSize() {
    var someList = new ArrayList<Hashable>();

    utils.compressedByteSize(someList);

    verify(compressor).compressedByteSize(eq(someList));
  }

}