package com.flockinger.groschn.blockchain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.impl.MultiHashGenerator;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MerkleRootCalculator.class, MultiHashGenerator.class})
@Import(CryptoConfig.class)
public class MerkleRootCalculatorTest {
  
  @Autowired
  private MerkleRootCalculator calc;
  
  @Test
  public void testCalculateMerkleRootHash_withFullListOfObjects_shouldReturnCorrect() {
    String rootHash = calc.calculateMerkleRootHash(fakeTransactions(9,false));
    
    assertNotNull("verify root hash is not null", rootHash);
    assertEquals("verify that root hash is correct", "0b1fc0b715863bd080af09be0bfffc7d196db47cb534fb6afe2c36044826ab55206d65c3f2949074e1259332b5c0117af8fb77a7595abb26552ad08e32bf9935",
        rootHash);
    
    String rootHashOneTransactionLess = calc.calculateMerkleRootHash(fakeTransactions(8, false));
    assertNotEquals("verify with one transaction missing the root is different", 
        rootHash, rootHashOneTransactionLess);
    
    String rootHashSlightlyModified = calc.calculateMerkleRootHash(fakeTransactions(9, true));
    assertNotEquals("verify that with the slightest change the outcome is different", 
        rootHash, rootHashSlightlyModified);
  }
  
  @Test
  public void testCalculateMerkleRootHash_withOneObject_shouldReturnCorrect() {
    String rootHash = calc.calculateMerkleRootHash(fakeTransactions(1,false));
    assertNotNull("verify root hash is not null", rootHash);
    assertFalse("verify that root hash is correct", rootHash.isEmpty());
  }
  
  @Test(expected = HashingException.class)
  public void testCalculateMerkleRootHash_withEmptyList_shouldStillReturnSomething() {
    calc.calculateMerkleRootHash(new ArrayList<>());
  }
  
  public static List<Transaction> fakeTransactions(int size, boolean modifyLittleStuff) {
    Transaction tra1 = new Transaction();
    tra1.setInputs(ImmutableList.of(fakeInput(86l), fakeInput(14l)));
    tra1.setOutputs(ImmutableList.of(fakeInput(27l), fakeInput(73l)));
    tra1.setLockTime(934857l);
    
    Transaction tra2 = new Transaction();
    tra2.setInputs(ImmutableList.of(fakeInput(6l), fakeInput(4l)));
    tra2.setOutputs(ImmutableList.of(fakeInput(7l), fakeInput(3l)));
    tra2.setLockTime(87687l);
    
    Transaction tra3 = new Transaction();
    tra3.setInputs(ImmutableList.of(fakeInput(9996l), fakeInput(9994l)));
    tra3.setOutputs(ImmutableList.of(fakeInput(9997l), fakeInput(9993l)));
    tra3.setLockTime(432l);
    
    Transaction tra4 = new Transaction();
    tra4.setInputs(ImmutableList.of(fakeInput(670006l), fakeInput(670004l)));
    tra4.setOutputs(ImmutableList.of(fakeInput(670007l), fakeInput(670003l)));
    tra4.setLockTime(987l);
    
    Transaction tra5 = new Transaction();
    tra5.setInputs(ImmutableList.of(fakeInput(3406l), fakeInput(3404l)));
    tra5.setOutputs(ImmutableList.of(fakeInput(3407l), fakeInput(3403l)));
    tra5.setLockTime(46547l);
    
    Transaction tra6 = new Transaction();
    tra6.setInputs(ImmutableList.of(fakeInput(106l), fakeInput(104l)));
    tra6.setOutputs(ImmutableList.of(fakeInput(107l), fakeInput(103l)));
    tra6.setLockTime(798678l);
    
    Transaction tra7 = new Transaction();
    tra7.setInputs(ImmutableList.of(fakeInput(6006l), fakeInput(6004l)));
    tra7.setOutputs(ImmutableList.of(fakeInput(6007l), fakeInput(6003l)));
    tra7.setLockTime(5423454l);
    
    Transaction tra8 = new Transaction();
    tra8.setInputs(ImmutableList.of(fakeInput(5006l), fakeInput(5004l)));
    tra8.setOutputs(ImmutableList.of(fakeInput(5007l), fakeInput(5003l)));
    tra8.setLockTime(5423454l);
    
    Transaction tra9 = new Transaction();
    tra9.setInputs(ImmutableList.of(fakeInput(4006l), fakeInput(4004l)));
    tra9.setOutputs(ImmutableList.of(fakeInput(4007l), fakeInput(4003l)));
    tra9.setLockTime(5423454l);
    
    if(modifyLittleStuff) {
      tra9.getOutputs().get(0).setTimestamp(1234568l);
    }
    
    List<Transaction> transactions = new ArrayList<>();
    transactions.addAll(ImmutableList.of(tra1, tra2, tra3, tra4, tra5));
    transactions.addAll(ImmutableList.of(tra6, tra7, tra8, tra9));
    return transactions.subList(0, size);
  }
  
  public static TransactionInput fakeInput(long amount) {
    TransactionInput input = new TransactionInput();
    input.setAmount(new BigDecimal(amount));
    input.setPublicKey("keykey");
    input.setTimestamp(1234567l);
    
    return input;
  }
}
