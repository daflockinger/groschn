package com.flockinger.groschn.blockchain.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.commons.ValidationUtils;
import com.flockinger.groschn.commons.exception.crypto.CantConfigureSigningAlgorithmException;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TransactionValidator.class, TransactionValidationHelper.class})
public class TransactionValidatorTest {
  
  @MockBean
  private ValidationUtils validationUtils;
  @MockBean
  private WalletService wallet;
  
  @Autowired
  private TransactionValidator validator;
  
  @Test
  public void testValidate_withValidTransaction_shouldReturnTrue() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify good transaction validated true", true, result.isValid());
    
    verify(validationUtils).isHashCorrect(any(), any());
    verify(validationUtils, times(3)).isSignatureValid(any(), any(), any());
    verify(wallet, times(3)).calculateBalance(any());
  }
  
  @Test
  public void testValidate_withWrongTransactionHash_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(false);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "hash"));
  }
  
  @Test
  public void testValidate_withInvalidTransactionHash_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenThrow(HashingException.class);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withInvalidTransactionsOutputHash_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenThrow(HashingException.class);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withWrongSignature_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(false);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "signature"));
  }
  
  @Test
  public void testValidate_withInvalidInputPublicKey_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenThrow(CantConfigureSigningAlgorithmException.class);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withEqualTransactionBalance_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal("100"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withHigherTransactionOutputSumThanInputSum_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal("101"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withNullInputs_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setAmount(null);
    transaction.getInputs().get(1).setAmount(null);
    transaction.getInputs().get(2).setAmount(null);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withInputSequenceHavingAGap_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(2).setSequenceNumber(4l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOutputSequenceHavingAGap_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(2).setSequenceNumber(4l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOutputsStartingByZero_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
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
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
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
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(1).setTimestamp(new Date().getTime() + 1000l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "future"));
  }
  
  @Test
  public void testValidate_withOneInputTimestampInFuture_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(1).setTimestamp(new Date().getTime() + 1000l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "future"));
  }
  
  @Test
  public void testValidate_withOneInputAmountZero_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setAmount(BigDecimal.ZERO);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneOutputAmountZero_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(BigDecimal.ZERO);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneInputAmountTooHigh_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setAmount(new BigDecimal(Block.MAX_AMOUNT_MINED_GROSCHN));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneOutputAmountTooHigh_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal(Block.MAX_AMOUNT_MINED_GROSCHN));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneInputFundTooLow_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("99"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "balance"));
  }
  
  @Test
  public void testValidate_withOneInputFundTooHigh_shouldReturnFalse() {
    Transaction transaction = createValidTransaction();
    when(validationUtils.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(validationUtils.generateListHash(any())).thenReturn(new byte[0]);
    when(validationUtils.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret1"))).thenReturn(new BigDecimal("101"));
    when(wallet.calculateBalance(matches("very-secret4"))).thenReturn(new BigDecimal("200"));
    when(wallet.calculateBalance(matches("very-secret3"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "balance"));
  }
  
  
  private Transaction createValidTransaction() {
    return TestDataFactory.createValidTransaction("very-secret1", "very-secret4", "very-secret3", "someone-else");
  }
}
