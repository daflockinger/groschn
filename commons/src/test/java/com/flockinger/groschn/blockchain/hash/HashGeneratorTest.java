package com.flockinger.groschn.blockchain.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.util.hash.HashGenerator;
import com.flockinger.groschn.blockchain.util.hash.MultiHashGenerator;
import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes = {MultiHashGenerator.class})
@RunWith(SpringRunner.class)
@Import({CryptoConfig.class, TestConfig.class})
public class HashGeneratorTest {

  @Autowired
  private HashGenerator hasher;

  @Test
  public void testGenerateHash_withValidHashableData_shouldCreateCorrectly()
      throws InterruptedException {
    Hashable hashable = createTestData(123l);

    String generatedHash = hasher.generateHash(hashable);
    String expectedHash =
        "6211819ca669ddd865cc64763a79121407517c0e1c4ad9a6613180f5a587963d63a255d018036f58600cb43c296c6826c133487037dc06bfeaa0aa8374828bd3";

    assertNotNull("verify returned hash is not null", generatedHash);
    assertEquals("verify correct generated hash", expectedHash, generatedHash);
  }
  
  @Test
  public void testGenerateHash_withVerifyThatSlightlyChangesHashesAreVeryDifferent_shouldCreateCorrectly()
      throws InterruptedException {
    for (int i = 0; i < 100; i++) {
      Thread.sleep(5);

      Hashable hashable = createTestData(new Date().getTime());

      String generatedHash = hasher.generateHash(hashable);

      assertNotNull("verify returned hash is not null", generatedHash);
      LevenshteinDistance levDistance = new LevenshteinDistance();
      Block differentBlock = createTestData(new Date().getTime() + 1);
      String slightlyModifiedHash = hasher.generateHash(differentBlock);
      Integer distance = levDistance.apply(generatedHash, slightlyModifiedHash);
      assertTrue("verify that the slightly modified Block-data generates completly different hash",
          distance > (generatedHash.length() * 0.75));
    }
  }
  
  @Test
  public void testGenerateHash_withGenerateTwice_shouldCreateSameHash() {
    Hashable hashable = createTestData(123l);
    String generatedHash = hasher.generateHash(hashable);

    assertNotNull("verify returned hash is not null", generatedHash);
    assertEquals("verify that generating the hash twice generates the same result", generatedHash,
        hasher.generateHash(hashable));
  }

  @Test
  public void testGenerateHash_withEmptyHashable_shouldStillCreateCorrectly() {
    String generatedHash = hasher.generateHash(new Block());

    assertNotNull("verify returned hash is not null", generatedHash);
  }
  
  @Test
  public void testGenerateListHash_withValidHashablesData_shouldCreateCorrectly()
      throws InterruptedException {
    TransactionOutput out = createTestOutput(12l);
    TransactionOutput ou3 = createTestOutput(null);
    TransactionOutput out2 = createTestOutput(14l);

    var outs = new ArrayList<TransactionOutput>();
    outs.addAll(ImmutableList.of(out, out2, ou3));
    byte[] generatedHash = hasher.generateListHash(outs);
    byte[] expectedHash = Hex.decode("0d103325df20dcf7c2b1d869c7f3d5dbe73806bffe802bf3a5eaedbdf79f069ccd134bcf03834ae3a8772cde141fa8a7d46365be3ef786347662fc2e761a859b");
    assertNotNull("verify returned hash is not null", generatedHash);
    assertTrue("verify correct generated hash", Arrays.equals(expectedHash, generatedHash));
    
    var outs2 = new ArrayList<TransactionOutput>();
    outs2.addAll(ImmutableList.of(out2, out, ou3));
    byte[] switchedHash = hasher.generateListHash(outs2);
    
    assertTrue("verify switched hash is still the same", Arrays.equals(generatedHash, switchedHash));
  }
  
  @Test
  public void testGenerateListHash_withSlightChangeValidHashablesData_shouldCreateCorrectly()
      throws InterruptedException {
    TransactionOutput out = createTestOutput(12l);
    TransactionOutput ou3 = createTestOutput(null);
    TransactionOutput out2 = createTestOutput(15l);

    var outs = new ArrayList<TransactionOutput>();
    outs.addAll(ImmutableList.of(out, out2, ou3));
    byte[] generatedHash = hasher.generateListHash(outs);
    byte[] expectedHash = Hex.decode("3f2db885499fb5cc776bfa846ba6341c1ae7d988943e8629c03758f4711a5fb99e820b76ef9bb379afb477486bf57f71b4d60458066195a231dc3340819944b2");
    
    assertNotNull("verify returned hash is not null", generatedHash);
    assertNotEquals("verify generated hash is different", expectedHash, generatedHash);
  }
  
  @Test
  public void testIsHashCorrect_withGenesisBlock_shouldReturnTrue() {
    Block genesis = Block.GENESIS_BLOCK();
    genesis.setHash(null);

    assertEquals("verify that correct hash for hashable returns true", true, 
        hasher.isHashCorrect(Block.GENESIS_BLOCK().getHash(), genesis));
  }
  
  @Test
  public void testIsHashCorrect_withCorrectHashAndHashable_shouldReturnTrue() {
    Hashable hashable = createTestData(123l);
    String hash = "6211819ca669ddd865cc64763a79121407517c0e1c4ad9a6613180f5a587963d63a255d018036f58600cb43c296c6826c133487037dc06bfeaa0aa8374828bd3";
    System.out.println((hasher.generateHash(hashable)));

    assertEquals("verify that correct hash for hashable returns true", true, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withCorrectHashAndHashableSomeUpperCase_shouldReturnTrue() {
    Hashable hashable = createTestData(123l);
    String hash = "6211819CA669DDD865CC64763A79121407517C0E1C4AD9A6613180f5A587963D63A255D018036F58600CB43C296C6826C133487037DC06BFEAA0AA8374828BD3";

    assertEquals("verify that correct hash for hashable returns true", true, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withSlightlyModifiedHash_shouldReturnFalse() {
    Hashable hashable = createTestData(123l);
    String hash = "fad4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

    assertEquals("verify that slightly modified hash returns false", false, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withSlightlyModifiedHashableValue_shouldReturnFalse() {
    Hashable hashable = createTestData(124l);
    String hash = "ead4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

    assertEquals("verify that slightly modified hashable value returns false", false, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withInvalidHash_shouldReturnFalse() {
    Hashable hashable = createTestData(123l);
    String hash = "ZZd4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

    assertEquals("verify with invalid hash returns false", false, 
        hasher.isHashCorrect(hash, hashable));
  }

  private Block createTestData(long timestamp) {
    Block block = new Block();
    block.setLastHash("123456");
    block.setPosition(2l);
    block.setTimestamp(timestamp);
    block.setTransactions(new ArrayList<>());
    block.setVersion(1);

    return block;
  }
  
  private TransactionOutput createTestOutput(Long sequenceNumber) {
    TransactionOutput out = new TransactionOutput();
    out.setAmount(new BigDecimal("123"));
    out.setPublicKey("master-key");
    out.setSequenceNumber(sequenceNumber);
    out.setTimestamp(1234l);
    return out;
  }
}
