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
import java.math.BigInteger;
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
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidationHelper;
import com.flockinger.groschn.blockchain.wallet.WalletService;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RewardTransactionValidator.class, TransactionValidationHelper.class})
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
  
  //TODO check if really everything is tested
  //change the double tests with regular transaction validator if possible
  
  @Test
  public void testValidate_withValidNormalAndRewardTransaction_shouldReturnTrue() {
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
  public void testValidate_withValidRewardOnlyTransaction_shouldReturnTrue() {
    Transaction transaction = createRewardOnlyTransaction();
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
    Transaction transaction = createRewardOnlyTransaction();
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
    Transaction transaction = createRewardOnlyTransaction();
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
    Transaction transaction = createRewardOnlyTransaction();
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
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("500"));
    
    transaction.getInputs().get(2).setAmount(new BigDecimal("500"));
    
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
  public void testValidate_withFaking2ndRewardDoubleSpend_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    TransactionInput input4 = new TransactionInput();
    input4.setAmount(new BigDecimal("100"));
    input4.setPublicKey("very-secret2");
    input4.setSequenceNumber(4l);
    input4.setSignature("x1x1x1");
    input4.setTimestamp(new Date().getTime() - 100l);
    transaction.getInputs().add(input4);
    transaction.getOutputs().get(3).setAmount(new BigDecimal("112"));
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "contains double spend"));
  }
  
  
  @Test
  public void testValidate_withCreateSimpleDoubleSpend_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("10"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    TransactionInput input4 = new TransactionInput();
    input4.setAmount(new BigDecimal("10"));
    input4.setPublicKey("very-secret2");
    input4.setSequenceNumber(4l);
    input4.setSignature("x1x1x1");
    input4.setTimestamp(new Date().getTime() - 100l);
    transaction.getInputs().add(input4);
    transaction.getInputs().get(1).setAmount(new BigDecimal("10"));
    transaction.getOutputs().get(3).setAmount(new BigDecimal("62"));
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "contains double spend"));
  }
  
  @Test
  public void testValidate_withMultipleRegularMinerInputs_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"))
      .thenReturn(new BigDecimal("300"));
    
    TransactionInput input4 = new TransactionInput();
    input4.setAmount(new BigDecimal("300"));
    input4.setPublicKey("minerKey");
    input4.setSequenceNumber(4l);
    input4.setSignature("x1x1x1");
    input4.setTimestamp(new Date().getTime() - 100l);
    transaction.getInputs().add(input4);
    transaction.getOutputs().get(3).setAmount(new BigDecimal("312"));
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "Besides the reward Transaction"));
  }
  
  @Test
  public void testValidate_withTwoTimesARewardMinerInputs_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"))
      .thenReturn(new BigDecimal("100"));
    
    TransactionInput input4 = new TransactionInput();
    input4.setAmount(new BigDecimal("100"));
    input4.setPublicKey("minerKey");
    input4.setSequenceNumber(4l);
    input4.setSignature("x1x1x1");
    input4.setTimestamp(new Date().getTime() - 100l);
    transaction.getInputs().add(input4);
    transaction.getOutputs().get(3).setAmount(new BigDecimal("312"));
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "exactly one miner"));
  }
  
  @Test
  public void testValidate_withWrongAmountRewardOutput_shouldReturnFalse() {
    Transaction transaction = createRewardOnlyTransaction();
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
    Transaction transaction = createRewardOnlyTransaction();
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
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
    transaction.getOutputs().get(1).setAmount(new BigDecimal("111"));
    transaction.getOutputs().remove(3);
        
    Assessment result = validator.validate(transaction);
    assertNotNull("verify assessment is not null", result);
    assertEquals("verify transaction validated false", false, result.isValid());
    assertTrue("verify correct error message", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), 
            "must contain one Reward Transaction Output and another one for the Change"));
  }
  
  
  
  @Test
  public void testValidate_withWrongTransactionHash_shouldReturnFalse() {
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
    when(hasher.isHashCorrect(matches("0FABDD34578"), any())).thenReturn(true);
    when(hasher.generateListHash(any())).thenReturn(new byte[0]);
    when(signer.isSignatureValid(any(), any(), any())).thenReturn(true);
    when(wallet.calculateBalance(matches("very-secret2"))).thenReturn(new BigDecimal("100"));
    when(wallet.calculateBalance(matches("minerKey"))).thenReturn(new BigDecimal("300"));
    
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    Transaction transaction = createRewardTransaction();
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
    out2.setAmount(new BigDecimal("99"));
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
  
  private Transaction createRewardOnlyTransaction() {
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
    transaction.setInputs(inputs);
    List<TransactionOutput> outputs = new ArrayList<>();
    TransactionOutput out1 = new TransactionOutput(); // reward
    out1.setAmount(new BigDecimal("100"));
    out1.setPublicKey("minerKey");
    out1.setSequenceNumber(1l);
    out1.setTimestamp(new Date().getTime() - 5000l);
    outputs.add(out1);
    TransactionOutput out4 = new TransactionOutput(); //change
    out4.setAmount(new BigDecimal("12"));
    out4.setPublicKey("minerKey");
    out4.setSequenceNumber(2l);
    out4.setTimestamp(new Date().getTime() - 25l);
    outputs.add(out4);
    transaction.setOutputs(outputs);
    return transaction;
  }
  
  private Transaction createDualRewardTx() {
    Transaction transaction = createRewardOnlyTransaction();
    Transaction secondTx = createRewardOnlyTransaction();
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
