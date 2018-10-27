package com.flockinger.groschn.blockchain.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.flockinger.groschn.blockchain.TestDataFactory;

public class BlockTest {
  
  @Test
  public void testToString() {
    Block block = TestDataFactory.getFakeBlock();
    block.getTransactions().get(0).setTransactionHash("Thash1");
    block.getTransactions().get(0).getOutputs().forEach(out -> out.setSequenceNumber(1l));
    block.getTransactions().get(1).setTransactionHash("Thash2");
    block.getTransactions().get(1).getOutputs().forEach(out -> out.setSequenceNumber(2l));
    final String wantedToStringResult = "Block [position=97, hash=0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938, lastHash=000cf8761b99932db819909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938, transactionMerkleRoot=9678087, timestamp=20000, version=1, consent=Consent [nonce=123, timestamp=2342343545, difficulty=5, milliSecondsSpentMining=12000, type=PROOF_OF_WORK], transactions=[Transaction [lockTime=934857, inputs=[TransactionInput [signature=xxx, amount=86, publicKey=keykey, timestamp=1234567, sequenceNumber=3], TransactionInput [signature=xxx, amount=14, publicKey=keykey, timestamp=1234567, sequenceNumber=3]], outputs=[TransactionOutput [amount=27, publicKey=keykey, timestamp=1234567, sequenceNumber=1], TransactionOutput [amount=73, publicKey=keykey, timestamp=1234567, sequenceNumber=1]], transactionHash=Thash1], Transaction [lockTime=87687, inputs=[TransactionInput [signature=xxx, amount=6, publicKey=keykey, timestamp=1234567, sequenceNumber=3], TransactionInput [signature=xxx, amount=4, publicKey=keykey, timestamp=1234567, sequenceNumber=3]], outputs=[TransactionOutput [amount=7, publicKey=keykey, timestamp=1234567, sequenceNumber=2], TransactionOutput [amount=3, publicKey=keykey, timestamp=1234567, sequenceNumber=2]], transactionHash=Thash2]]]";
    
    assertEquals("verify correct to string result conains everything",  
        wantedToStringResult, block.toString());
  }
  
  
  @Test
  public void testCompareTo_withEqualPositionBlocks() {
    Block block1 = TestDataFactory.getFakeBlock();
    block1.setPosition(2l);
    Block block2 = TestDataFactory.getFakeBlock();
    block2.setPosition(2l);
    
    assertEquals("verify same position blocks compare 0", 0, block1.compareTo(block2));
  }
  
  @Test
  public void testCompareTo_withInitialBlockHigherPos() {
    Block block1 = TestDataFactory.getFakeBlock();
    block1.setPosition(3l);
    Block block2 = TestDataFactory.getFakeBlock();
    block2.setPosition(2l);
    
    assertEquals("verify same position blocks compare 0", 1, block1.compareTo(block2));
  }
  
  @Test
  public void testCompareTo_withInitialBlockLowerPos() {
    Block block1 = TestDataFactory.getFakeBlock();
    block1.setPosition(1l);
    Block block2 = TestDataFactory.getFakeBlock();
    block2.setPosition(2l);
    
    assertEquals("verify same position blocks compare 0", -1, block1.compareTo(block2));
  }
}
