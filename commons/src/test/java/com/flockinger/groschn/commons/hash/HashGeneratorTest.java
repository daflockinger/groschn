package com.flockinger.groschn.commons.hash;

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
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.TestConfig;
import com.flockinger.groschn.commons.config.CommonsConfig;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.model.TestBlock;
import com.flockinger.groschn.commons.model.TestTransactionOutput;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@Import({CommonsConfig.class, TestConfig.class})
public class HashGeneratorTest {

  @Autowired
  private HashGenerator hasher;

  @Test
  public void testGenerateHash_withValidHashableData_shouldCreateCorrectly()
      throws InterruptedException {
    Hashable<TestBlock> hashable = createTestData(123l);

    String generatedHash = hasher.generateHash(hashable);
    String expectedHash =
        "1df4d28a29c655d93bc19e18506b5fb29c9aec9650922e95df86ffab46617820c18ac1ac9780e4a6843c62bddebdf17736769473fde246eedec32491018b0e5c";

    assertNotNull("verify returned hash is not null", generatedHash);
    assertEquals("verify correct generated hash", expectedHash, generatedHash);
  }
  
  @Test
  public void testGenerateHash_withVerifyThatSlightlyChangesHashesAreVeryDifferent_shouldCreateCorrectly()
      throws InterruptedException {
    for (int i = 0; i < 100; i++) {
      Thread.sleep(5);

      Hashable<TestBlock> hashable = createTestData(new Date().getTime());

      String generatedHash = hasher.generateHash(hashable);

      assertNotNull("verify returned hash is not null", generatedHash);
      LevenshteinDistance levDistance = new LevenshteinDistance();
      TestBlock differentBlock = createTestData(new Date().getTime() + 1);
      String slightlyModifiedHash = hasher.generateHash(differentBlock);
      Integer distance = levDistance.apply(generatedHash, slightlyModifiedHash);
      assertTrue("verify that the slightly modified Block-data generates completly different hash",
          distance > (generatedHash.length() * 0.75));
    }
  }
  
  @Test
  public void testGenerateHash_withGenerateTwice_shouldCreateSameHash() {
    Hashable<TestBlock> hashable = createTestData(123l);
    String generatedHash = hasher.generateHash(hashable);

    assertNotNull("verify returned hash is not null", generatedHash);
    assertEquals("verify that generating the hash twice generates the same result", generatedHash,
        hasher.generateHash(hashable));
  }

  @Test
  public void testGenerateHash_withEmptyHashable_shouldStillCreateCorrectly() {
    String generatedHash = hasher.generateHash(new TestBlock());

    assertNotNull("verify returned hash is not null", generatedHash);
  }
  
  @Test
  public void testGenerateListHash_withValidHashablesData_shouldCreateCorrectly()
      throws InterruptedException {
    TestTransactionOutput out = createTestOutput(12l);
    TestTransactionOutput ou3 = createTestOutput(null);
    TestTransactionOutput out2 = createTestOutput(14l);

    var outs = new ArrayList<TestTransactionOutput>();
    outs.addAll(ImmutableList.of(out, out2, ou3));
    byte[] generatedHash = hasher.generateListHash(outs);    
    byte[] expectedHash = Hex.decode("264102823653e9d715f41899e1e31e8cad53ab34d418e0d3d80f4dbff15cbb8d7b42492d32d09c6ef5744e9dec248644103811e2adb5e152557247e72ccc783a");
    assertNotNull("verify returned hash is not null", generatedHash);
    assertTrue("verify correct generated hash", Arrays.equals(expectedHash, generatedHash));
    
    var outs2 = new ArrayList<TestTransactionOutput>();
    outs2.addAll(ImmutableList.of(out2, out, ou3));
    byte[] switchedHash = hasher.generateListHash(outs2);
    
    assertTrue("verify switched hash is still the same", Arrays.equals(generatedHash, switchedHash));
  }
  
  @Test
  public void testGenerateListHash_withSlightChangeValidHashablesData_shouldCreateCorrectly()
      throws InterruptedException {
    TestTransactionOutput out = createTestOutput(12l);
    TestTransactionOutput ou3 = createTestOutput(null);
    TestTransactionOutput out2 = createTestOutput(15l);

    var outs = new ArrayList<TestTransactionOutput>();
    outs.addAll(ImmutableList.of(out, out2, ou3));
    byte[] generatedHash = hasher.generateListHash(outs);
    byte[] expectedHash = Hex.decode("3f2db885499fb5cc776bfa846ba6341c1ae7d988943e8629c03758f4711a5fb99e820b76ef9bb379afb477486bf57f71b4d60458066195a231dc3340819944b2");
    
    assertNotNull("verify returned hash is not null", generatedHash);
    assertNotEquals("verify generated hash is different", expectedHash, generatedHash);
  }
  
  @Test
  public void testIsHashCorrect_withGenesisBlock_shouldReturnTrue() {
    TestBlock genesis = TestBlock.GENESIS_BLOCK();
    genesis.setHash(null);

    assertEquals("verify that correct hash for hashable returns true", true, 
        hasher.isHashCorrect(TestBlock.GENESIS_BLOCK().getHash(), genesis));
  }
  
  @Test
  public void testIsHashCorrect_withCorrectHashAndHashable_shouldReturnTrue() {
    Hashable<TestBlock> hashable = createTestData(123l);
    String hash = "1df4d28a29c655d93bc19e18506b5fb29c9aec9650922e95df86ffab46617820c18ac1ac9780e4a6843c62bddebdf17736769473fde246eedec32491018b0e5c";
    
    assertEquals("verify that correct hash for hashable returns true", true, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withCorrectHashAndHashableSomeUpperCase_shouldReturnTrue() {
    Hashable<TestBlock> hashable = createTestData(123l);
    String hash = "1df4d28a29c655d93bc19e18506b5FB29c9AEC9650922e95df86ffab46617820c18ac1ac9780e4a6843c62bddebdf17736769473FDE246eedec32491018b0e5c";

    assertEquals("verify that correct hash for hashable returns true", true, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withSlightlyModifiedHash_shouldReturnFalse() {
    Hashable<TestBlock> hashable = createTestData(123l);
    String hash = "fad4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

    assertEquals("verify that slightly modified hash returns false", false, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withSlightlyModifiedHashableValue_shouldReturnFalse() {
    Hashable<TestBlock> hashable = createTestData(124l);
    String hash = "ead4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

    assertEquals("verify that slightly modified hashable value returns false", false, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withInvalidHash_shouldReturnFalse() {
    Hashable<TestBlock> hashable = createTestData(123l);
    String hash = "ZZd4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

    assertEquals("verify with invalid hash returns false", false, 
        hasher.isHashCorrect(hash, hashable));
  }

  private TestBlock createTestData(long timestamp) {
    TestBlock block = new TestBlock();
    block.setLastHash("123456");
    block.setPosition(2l);
    block.setTimestamp(timestamp);
    block.setTransactions(new ArrayList<>());
    block.setVersion(1);

    return block;
  }
  
  private TestTransactionOutput createTestOutput(Long sequenceNumber) {
    TestTransactionOutput out = new TestTransactionOutput();
    out.setAmount(new BigDecimal("123"));
    out.setPublicKey("master-key");
    out.setSequenceNumber(sequenceNumber);
    out.setTimestamp(1234l);
    return out;
  }
}
