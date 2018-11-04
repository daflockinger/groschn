package com.flockinger.groschn.blockchain.validation;

import static com.flockinger.groschn.blockchain.TestDataFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.validation.impl.RewardTransactionValidator;
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidationHelper;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.commons.exception.crypto.CantConfigureSigningAlgorithmException;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RewardTransactionValidator.class, TransactionValidationHelper.class})
public class RewardTransactionValidatorTest {
  
  @MockBean
  private HashGenerator hasher;
  @MockBean
  private Signer signer;
  @MockBean
  private WalletService wallet;
  @MockBean
  private Bookkeeper bookKeeper;
  
  @Autowired
  private RewardTransactionValidator validator;
  
  @Before
  public void setup() {
    when(bookKeeper.calculateCurrentBlockReward()).thenReturn(new BigDecimal("100"));
  }
    
  @Test
  public void testValidate_withValidNormalAndRewardTransaction_shouldReturnTrue() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("400"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify good transaction validated true", true, result.isValid());
    
    verify(hasher).isHashCorrect(any(), any());
    verify(signer, times(2)).isSignatureValid(any(), any(), any());
    verify(wallet, times(1)).calculateBalance(any());
  }
  
  @Test
  public void testValidate_withValidRewardOnlyTransaction_shouldReturnTrue() {
    Transaction transaction = createRewardTransaction(true);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify good transaction validated true", true, result.isValid());
    
    verify(hasher).isHashCorrect(any(), any());
    verify(signer, times(1)).isSignatureValid(any(), any(), any());
  }
  
  
  @Test
  public void testValidate_withWrongInputRewardValue_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(true);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    
    transaction.getInputs().get(0).setAmount(new BigDecimal("99"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "exactly one"));
  }
  
  @Test
  public void testValidate_withWrongOutputRewardValue_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(true);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal("99"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "exactly one"));
  }
  
  @Test
  public void testValidate_withWrongOutAndInputRewardValue_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(true);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal("99"));
    transaction.getInputs().get(0).setAmount(new BigDecimal("99"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "exactly one"));
  }
  
  
  @Test
  public void testValidate_withHigherMinerInThanOutputSum_shouldReturnfalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("500"));
    
    transaction.getInputs().get(1).setAmount(new BigDecimal("500"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "must be higher or equal"));
  }
  
  @Test
  public void testValidate_withTwoRewards_shouldReturnFalse() {
    Transaction transaction = createDualRewardTx();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "exactly one"));
  }
  
  
  @Test
  public void testValidate_withMultipleRegularMinerInputs_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("400"))
      .thenReturn(new BigDecimal("400"));
    
    TransactionInput input4 = new TransactionInput();
    input4.setAmount(new BigDecimal("400"));
    input4.setPublicKey("minerKey");
    input4.setSequenceNumber(3l);
    input4.setSignature("x1x1x1");
    input4.setTimestamp(new Date().getTime() - 100l);
    transaction.getInputs().add(input4);
    transaction.getOutputs().get(1).setAmount(new BigDecimal("812"));
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "Besides the reward Transaction"));
  }
  
  @Test
  public void testValidate_withWrongAmountRewardOutput_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(true);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal("94"));
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withNoChangeOutput_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(true);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    
    transaction.getOutputs().remove(1);
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), 
            "must contain one Reward Transaction Output and another one for the Change"));
  }
  
  @Test
  public void testValidate_withNoChangeOutputButNormalAndRewardTransaction_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("400"));
    
    transaction.getOutputs().get(1).setAmount(new BigDecimal("411"));
    transaction.getOutputs().remove(2);
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), 
            "must contain one Reward Transaction Output and another one for the Change"));
  }
  
  
  @Test
  public void testValidate_withNonMinerInAndOutput_shouldReturnfalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("400"));
    when(wallet.calculateBalance(matches("someoneHacker"))).thenReturn(BigDecimal.valueOf(12l));
    
    transaction.getInputs().add(mapToTransactionInput(createRandomTransactionInputWith(transaction.getInputs().size() + 1, "someoneHacker", 12l)));
    transaction.getOutputs().add(mapToTransactionOutput(createRandomTransactionOutputWith(transaction.getInputs().size() + 1, "someoneHacker", 12l)));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), 
            "other than from the miner"));
  }
  
  @Test
  public void testValidate_withWrongTransactionHash_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(false);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "hash"));
  }
  
  @Test
  public void testValidate_withInvalidTransactionHash_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenThrow(HashingException.class);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withInvalidTransactionsOutputHash_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenThrow(HashingException.class);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
  @Test
  public void testValidate_withWrongSignature_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(false);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "signature"));
  }
  
  @Test
  public void testValidate_withInvalidInputPublicKey_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenThrow(CantConfigureSigningAlgorithmException.class);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
  }
  
   
  @Test
  public void testValidate_withInputSequenceHavingAGap_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(1).setSequenceNumber(4l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOutputSequenceHavingAGap_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(2).setSequenceNumber(4l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOutputsStartingByZero_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
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
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setSequenceNumber(0l);
    transaction.getInputs().get(1).setSequenceNumber(1l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "sequence"));
  }
  
  @Test
  public void testValidate_withOneOutputTimestampInFuture_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(1).setTimestamp(new Date().getTime() + 1000l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "future"));
  }
  
  @Test
  public void testValidate_withOneInputTimestampInFuture_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(1).setTimestamp(new Date().getTime() + 1000l);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "future"));
  }
  
  @Test
  public void testValidate_withOneInputAmountZero_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setAmount(BigDecimal.ZERO);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneOutputAmountZero_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(BigDecimal.ZERO);
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneInputAmountTooHigh_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getInputs().get(0).setAmount(new BigDecimal(Block.MAX_AMOUNT_MINED_GROSCHN));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  @Test
  public void testValidate_withOneOutputAmountTooHigh_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction(false);
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(0).setAmount(new BigDecimal(Block.MAX_AMOUNT_MINED_GROSCHN));
    
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "amount"));
  }
  
  
  private Transaction createDualRewardTx() {
    Transaction transaction = createRewardTransaction(true);
    Transaction secondTx = createRewardTransaction(true);
    secondTx.getOutputs().get(0).setSequenceNumber(3l);
    secondTx.getOutputs().get(1).setSequenceNumber(4l);
    secondTx.getInputs().get(0).setSequenceNumber(2l);
    secondTx.getOutputs().forEach(out -> out.setPublicKey("2ndMinerKey"));
    secondTx.getInputs().forEach(in -> in.setPublicKey("2ndMinerKey"));
    transaction.getOutputs().addAll(secondTx.getOutputs());
    transaction.getInputs().addAll(secondTx.getInputs());
    return transaction;
  }
}
