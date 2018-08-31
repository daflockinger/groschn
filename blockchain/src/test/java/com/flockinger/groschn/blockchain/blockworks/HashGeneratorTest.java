package com.flockinger.groschn.blockchain.blockworks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.impl.MultiHashGenerator;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Hashable;

@ContextConfiguration(classes = {MultiHashGenerator.class})
@RunWith(SpringRunner.class)
@Import(CryptoConfig.class)
public class HashGeneratorTest {

  @Autowired
  private HashGenerator hasher;

  @Test
  public void testGenerateHash_withValidHashableData_shouldCreateCorrectly()
      throws InterruptedException {
    Hashable hashable = createTestData(123l);

    String generatedHash = hasher.generateHash(hashable);
    String expectedHash =
        "ead4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

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

  @Test(expected = HashingException.class)
  public void testGenerateHash_withHashableToByteArrayReturnsNull_shouldThrowException() {
    String generatedHash = hasher.generateHash(new Hashable() {
      public byte[] toByteArray() {
        return null;
      }
    });
    assertNotNull("verify returned hash is not null", generatedHash);
  }

  @Test(expected = HashingException.class)
  public void testGenerateHash_withHashableToByteArrayReturnsEmpty_shouldThrowException() {
    String generatedHash = hasher.generateHash(new Hashable() {
      public byte[] toByteArray() {
        return new byte[0];
      }
    });
    assertNotNull("verify returned hash is not null", generatedHash);
  }
  
  @Test
  public void testIsHashCorrect_withCorrectHashAndHashable_shouldReturnTrue() {
    Hashable hashable = createTestData(123l);
    String hash = "ead4bf68adc722df1dfadd5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

    assertEquals("verify that correct hash for hashable returns true", true, 
        hasher.isHashCorrect(hash, hashable));
  }
  
  @Test
  public void testIsHashCorrect_withCorrectHashAndHashableSomeUpperCase_shouldReturnTrue() {
    Hashable hashable = createTestData(123l);
    String hash = "EAD4BF68ADC722DF1DFADD5bf833b26579150e23bf865a1cc72ed39c394a3b26e22158877a7fa79f54a4614c65d3a502e71537f697f5987145f0d137a3e21e49";

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
}
