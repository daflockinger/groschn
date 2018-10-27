package com.flockinger.groschn.blockchain.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.flockinger.groschn.blockchain.TestDataFactory;

public class TransactionTest {

  
  @Test
  public void testToString() {
    Transaction transaction = TestDataFactory.fakeTransactions().get(0);
    transaction.setTransactionHash("Thash1");
    transaction.getOutputs().forEach(out -> out.setSequenceNumber(1l));
    final String wantedToStringResult = "Transaction [lockTime=934857, inputs=[TransactionInput [signature=xxx, amount=86, publicKey=keykey, timestamp=1234567, sequenceNumber=3], TransactionInput [signature=xxx, amount=14, publicKey=keykey, timestamp=1234567, sequenceNumber=3]], outputs=[TransactionOutput [amount=27, publicKey=keykey, timestamp=1234567, sequenceNumber=1], TransactionOutput [amount=73, publicKey=keykey, timestamp=1234567, sequenceNumber=1]], transactionHash=Thash1]";
    
    assertEquals("verify correct to string result conains everything",  
        wantedToStringResult, transaction.toString());
  }
  
  @Test
  public void testCompareTo_withEqualPositionBlocks() {
    Transaction transaction1 = TestDataFactory.fakeTransactions().get(0);
    transaction1.setTransactionHash("2");
    Transaction transaction2 = TestDataFactory.fakeTransactions().get(0);
    transaction2.setTransactionHash("2");
    
    assertEquals("verify same position blocks compare 0", 0, transaction1.compareTo(transaction2));
  }
  
  @Test
  public void testCompareTo_withInitialBlockHigherPos() {
    Transaction transaction1 = TestDataFactory.fakeTransactions().get(0);
    transaction1.setTransactionHash("3");
    Transaction transaction2 = TestDataFactory.fakeTransactions().get(0);
    transaction2.setTransactionHash("2");
    
    assertEquals("verify same position blocks compare 0", 1, transaction1.compareTo(transaction2));
  }
  
  @Test
  public void testCompareTo_withInitialBlockLowerPos() {
    Transaction transaction1 = TestDataFactory.fakeTransactions().get(0);
    transaction1.setTransactionHash("1");
    Transaction transaction2 = TestDataFactory.fakeTransactions().get(0);
    transaction2.setTransactionHash("2");
    
    assertEquals("verify same position blocks compare 0", -1, transaction1.compareTo(transaction2));
  }
}
