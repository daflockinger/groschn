package com.flockinger.groschn.blockchain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.consensus.model.PowConsent;
import com.flockinger.groschn.blockchain.model.Block;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CompressionUtils.class})
public class CompressionUtilsTest {

  @Autowired
  private CompressionUtils utils;
  
  @Test
  public void testCompressUncompress_withFullBlock_shouldDoCorrectly() {
    Block fakeBlock = getFakeBlock();
    
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 5594l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 761l, entity.getEntity().length);
    
    
    
    Optional<Block> uncompressedBlock = utils.decompress(entity.getEntity(), entity.getOriginalSize(), Block.class);
    // TODO MAKE BLOCK DESERIALIZABLE -> add specific implementation for Consent and not only naked interface 
    // assertTrue("verify that uncompressed Block is present", uncompressedBlock.isPresent());
    
    boolean bla = Objects.deepEquals(fakeBlock, fakeBlock);
    boolean bli = Objects.deepEquals(fakeBlock, uncompressedBlock);
    
    System.out.println();
  }
  
  private Block getFakeBlock() {
    Block block = new Block();
    block.setPosition(97l);
    block.setHash("0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    PowConsent powConsent = new PowConsent();
    powConsent.setDifficulty(5);
    powConsent.setMilliSecondsSpentMining(12000l);
    powConsent.setNonce(123l);
    powConsent.setTimestamp(2342343545l);
    block.setConsent(powConsent);
    block.setLastHash("000cf8761b99932db819909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    block.setTransactionMerkleRoot("9678087");
    block.setTransactions(MerkleRootCalculatorTest.fakeTransactions(9, true));
    return block;
  }
  
  
}
