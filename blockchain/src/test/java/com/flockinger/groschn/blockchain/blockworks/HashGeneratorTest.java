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
        "65fc00253d4893d28e8be00cfa6c9d36521bd77152a3f8441b97b6fbc029256d28d9fdb1a10d16bb262b2cefd6a1a3f16f5929c055aeecda5a45c02f722ed5f4";

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
