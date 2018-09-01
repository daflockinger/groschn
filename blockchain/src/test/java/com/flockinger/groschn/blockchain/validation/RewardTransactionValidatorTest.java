package com.flockinger.groschn.blockchain.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.exception.crypto.CantConfigureSigningAlgorithmException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.blockchain.validation.impl.RewardTransactionValidator;
import com.flockinger.groschn.blockchain.wallet.WalletService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RewardTransactionValidator.class, })
public class RewardTransactionValidatorTest {
  
  @MockBean
  private HashGenerator hasher;
  @MockBean(name="ECDSA_Signer")
  private Signer signer;
  @MockBean
  private WalletService wallet;
  @MockBean
  private BlockStorageService blockService;
  @MockBean
  private Bookkeeper bookKeeper;
  
  
  @Autowired
  private RewardTransactionValidator validator;
  
  @Before
  public void setup() {
    when(blockService.getLatestBlock()).thenReturn(Block.GENESIS_BLOCK());
    when(bookKeeper.calculateBlockReward(any())).thenReturn(new BigDecimal("100"));
  }
  
  //TODO add many more tests
  
  @Test
  public void testValidate_withValidTransaction_shouldReturnTrue() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify good transaction validated true", true, result.isValid());
    
    verify(hasher).isHashCorrect(any(), any());
    verify(signer, times(3)).isSignatureValid(any(), any(), any());
    verify(wallet, times(2)).calculateBalance(any());
  }
  
  @Test
  public void testValidate_withWrongTransactionHash_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(false);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "hash"));
  }
  
  @Test
  public void testValidate_withInvalidTransactionHash_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenThrow(HashingException.class);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withInvalidTransactionsOutputHash_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenThrow(HashingException.class);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withWrongSignature_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(false);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "signature"));
  }
  
  @Test
  public void testValidate_withInvalidInputPublicKey_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenThrow(CantConfigureSigningAlgorithmException.class);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withHigherTransactionOutputSumThanInputSum_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(1).setAmount(new BigDecimal("201"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
   
  @Test
  public void testValidate_withInputSequenceHavingAGap_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(2).setSequenceNumber(4l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOutputSequenceHavingAGap_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(2).setSequenceNumber(4l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOutputsStartingByZero_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setSequenceNumber(0l);
    transaction.getOutputs().get(1).setSequenceNumber(1l);
    transaction.getOutputs().get(2).setSequenceNumber(2l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withInputsStartingByZero_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setSequenceNumber(0l);
    transaction.getInputs().get(1).setSequenceNumber(1l);
    transaction.getInputs().get(2).setSequenceNumber(2l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOneOutputTimestampInFuture_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(1).setTimestamp(new Date().getTime() + 1000l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "future"));
  }
  
  @Test
  public void testValidate_withOneInputTimestampInFuture_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(1).setTimestamp(new Date().getTime() + 1000l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "future"));
  }
  
  @Test
  public void testValidate_withOneInputAmountZero_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setAmount(BigDecimal.ZERO);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneOutputAmountZero_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(BigDecimal.ZERO);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneInputAmountTooHigh_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setAmount(new BigDecimal(Block.MAX_AMOUNT_MINED_GROSCHN));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneOutputAmountTooHigh_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal(Block.MAX_AMOUNT_MINED_GROSCHN));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  
  private Transaction createRewardTransaction() {
    Transaction transaction = new Transaction();
    transaction.setId(UUID.randomUUID().toString());
    transaction.setTransactionHash("0FABDD34578");
    List<TransactionInput> inputs = new ArrayList<>();
    TransactionInput input1 = new TransactionInput();
    input1.setAmount(new BigDecimal("100"));
    input1.setPublicKey("minerKey");
    input1.setSequenceNumber(1l);
    input1.setSignature("x0x0x0");
    input1.setTimestamp(new Date().getTime() - 4000l);
    inputs.add(input1);
    TransactionInput input2 = new TransactionInput();
    input2.setAmount(new BigDecimal("100"));
    input2.setPublicKey("very-secret2");
    input2.setSequenceNumber(2l);
    input2.setSignature("x1x1x1");
    input2.setTimestamp(new Date().getTime() - 100l);
    inputs.add(input2);
    TransactionInput input3 = new TransactionInput();
    input3.setAmount(new BigDecimal("300"));
    input3.setPublicKey("minerKey");
    input3.setSequenceNumber(3l);
    input3.setSignature("x2x2x2");
    input3.setTimestamp(new Date().getTime() - 10l);
    inputs.add(input3);
    transaction.setInputs(inputs);
    List<TransactionOutput> outputs = new ArrayList<>();
    TransactionOutput out1 = new TransactionOutput(); // reward
    out1.setAmount(new BigDecimal("100"));
    out1.setPublicKey("minerKey");
    out1.setSequenceNumber(1l);
    out1.setTimestamp(new Date().getTime() - 5000l);
    outputs.add(out1);
    TransactionOutput out2 = new TransactionOutput();
    out2.setAmount(new BigDecimal("100"));
    out2.setPublicKey("very-secret2");
    out2.setSequenceNumber(2l);
    out2.setTimestamp(new Date().getTime() - 500l);
    outputs.add(out2);
    TransactionOutput out3 = new TransactionOutput();
    out3.setAmount(new BigDecimal("300"));
    out3.setPublicKey("minerKey");
    out3.setSequenceNumber(3l);
    out3.setTimestamp(new Date().getTime() - 50l);
    outputs.add(out3);
    TransactionOutput out4 = new TransactionOutput(); //change
    out4.setAmount(new BigDecimal("12"));
    out4.setPublicKey("minerKey");
    out4.setSequenceNumber(4l);
    out4.setTimestamp(new Date().getTime() - 25l);
    outputs.add(out4);
    transaction.setOutputs(outputs);
    return transaction;
  }
}
