package com.flockinger.groschn.commons;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.model.TestBlock;
import com.flockinger.groschn.commons.sign.Signer;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

public class BlockchainUtilsFactoryTest {

  @Test
  public void createHashGenerator() {
    var hashGen =  BlockchainUtilsFactory.createHashGenerator(getDefaultProvider());
    assertNotNull("verify it returned a hash generator instance", hashGen);

    var testBlock = new TestBlock();
    var blocks = new ArrayList<TestBlock>();
    blocks.add(testBlock);

    assertNotNull("merkle root generation should work", hashGen.calculateMerkleRootHash(blocks));
  }

  @Test
  public void createCompressor() {
    assertNotNull("verify it returned a valid compressor", BlockchainUtilsFactory.createCompressor(new ArrayList<>()));
  }

  @Test
  public void createSigner() {
    assertNotNull("verify it returned a valid signer", BlockchainUtilsFactory.createSigner(getDefaultProvider()));
  }

  @Test
  public void createCipher() {
    assertNotNull("verify it returned a valid cipher", BlockchainUtilsFactory.createCipher(getDefaultProvider()));
  }

  @Test
  public void buildTransactionUtils() {
    var transactionUtils = BlockchainUtilsFactory.buildTransactionUtils(mock(Signer.class), mock(HashGenerator.class), mock(Compressor.class));

    assertNotNull("verify it returned  non null TransactionUtils", transactionUtils);
  }

  @Test
  public void buildValidationUtils() {
    var validationUtils = BlockchainUtilsFactory.buildValidationUtils(mock(Signer.class), mock(HashGenerator.class), mock(Compressor.class));
    assertNotNull("verify it returned  non null ValidationUtils", validationUtils);
  }

  public Provider getDefaultProvider() {
    Provider bouncyCastle = new BouncyCastleProvider();
    Security.addProvider(bouncyCastle);
    return bouncyCastle;
  }
}