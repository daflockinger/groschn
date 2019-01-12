package com.flockinger.groschn.blockchain.validation.impl;

import static com.flockinger.groschn.blockchain.TestDataFactory.createBlockTransactions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockTransactionsValidator.class, TransactionValidationHelper.class})
public class BlockTransactionsValidatorTest {
  
  @MockBean(name = "Transaction_Validator")
  private Validator<Transaction> transactionValidator;
  @MockBean(name = "RewardTransaction_Validator")
  private Validator<Transaction> rewardTransactionValidator;
  
  @Autowired
  private BlockTransactionsValidator validator;
  
  @Test
  public void testValidate_withLotsOfTransactionsAndARewardWithNormalTransaction_shouldValidateTrue() {
    List<Transaction> transactions = createBlockTransactions(false, false);

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", true, result.isValid());
    
    verify(transactionValidator,times(11)).validate(any());
    ArgumentCaptor<Transaction> rewardCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(rewardTransactionValidator).validate(rewardCaptor.capture());
    Transaction reward = rewardCaptor.getValue();
    assertNotNull("verify that to validate reward is not null", reward);
    assertEquals("verify that to validate reward is correct", "minerKey", reward.getInputs().get(0).getPublicKey());
  }
  
  @Test
  public void testValidate_withRewardTxOnlyAndARewardWithNormalTransaction_shouldValidateTrue() {
    List<Transaction> transactions = createBlockTransactions(false, true);
    transactions.get(0).getOutputs().get(1).setAmount(new BigDecimal("388"));

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", true, result.isValid());
    
    verify(transactionValidator,times(0)).validate(any());
    ArgumentCaptor<Transaction> rewardCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(rewardTransactionValidator).validate(rewardCaptor.capture());
    Transaction reward = rewardCaptor.getValue();
    assertNotNull("verify that to validate reward is not null", reward);
    assertEquals("verify that to validate reward is correct", "minerKey", reward.getInputs().get(0).getPublicKey());
  }
  
  @Test
  public void testValidate_withhLotsOfTransactionsAndARewardOnlyTransaction_shouldValidateTrue() {
    List<Transaction> transactions = createBlockTransactions(true, false);
    transactions.add(TestDataFactory.createValidTransaction("special1", "special2", "special3", "special2"));

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", true, result.isValid());
    
    verify(transactionValidator,times(12)).validate(any());
    ArgumentCaptor<Transaction> rewardCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(rewardTransactionValidator).validate(rewardCaptor.capture());
    Transaction reward = rewardCaptor.getValue();
    assertNotNull("verify that to validate reward is not null", reward);
    assertEquals("verify that to validate reward is correct", "minerKey", reward.getInputs().get(0).getPublicKey());
  }
  
  /**
   * TODO check with reward validation that a Reward is valid also with only one miner-output!!
   */
  @Test
  public void testValidate_withhEmptyTransactionsMiningOnlyAndARewardOnlyTransaction_shouldValidateTrue() {
    List<Transaction> transactions = createBlockTransactions(true, true);
    transactions.get(0).getOutputs().remove(1);
    
    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", true, result.isValid());
    
    verify(transactionValidator,times(0)).validate(any());
    ArgumentCaptor<Transaction> rewardCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(rewardTransactionValidator).validate(rewardCaptor.capture());
    Transaction reward = rewardCaptor.getValue();
    assertNotNull("verify that to validate reward is not null", reward);
    assertEquals("verify that to validate reward is correct", "minerKey", reward.getInputs().get(0).getPublicKey());
  }
  
  @Test
  public void testValidate_withhTransactionsHavingMinersExpenseAndARewardOnlyTransaction_shouldValidateTrue() {
    List<Transaction> transactions = createBlockTransactions(true, false);
    transactions.add(TestDataFactory.createValidTransaction("special1", "special2", "special3", "special2"));
    transactions.get(1).getInputs().get(0).setPublicKey("minerKey");
    transactions.get(1).getOutputs().get(0).setPublicKey("minerKey");

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", true, result.isValid());
    
    verify(transactionValidator,times(12)).validate(any());
    ArgumentCaptor<Transaction> rewardCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(rewardTransactionValidator).validate(rewardCaptor.capture());
    Transaction reward = rewardCaptor.getValue();
    assertNotNull("verify that to validate reward is not null", reward);
    assertEquals("verify that to validate reward is correct", "minerKey", reward.getInputs().get(0).getPublicKey());
  }
  
  
  @Test
  public void testValidate_withhTransactionsHavingMinersExpenseAndARewardWithBalanceTransaction_shouldValidateFalseCauseDoubleSpend() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.get(1).getInputs().get(0).setPublicKey("minerKey");
    transactions.get(1).getOutputs().get(0).setPublicKey("minerKey");

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "must be unique for all transactions"));
  }
  
  
  
  @Test
  public void testValidate_withMoreOutputThanInputAmount_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.get(0).getOutputs().get(0).setAmount(new BigDecimal("100"));

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "must equal to"));
  }
  
  @Test
  public void testValidate_withMoreInputThanOutputAmount_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.get(0).getOutputs().get(0).setAmount(new BigDecimal("98"));

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "must equal to"));
  }
  
  @Test
  public void testValidate_withOneTransactionValidationFailing_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    when(transactionValidator.validate(any())).thenThrow(AssessmentFailedException.class);

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
  }
  
  @Test
  public void testValidate_withRewardTransactionValidationFailing_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    when(rewardTransactionValidator.validate(any())).thenThrow(AssessmentFailedException.class);

    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
  }
  
  @Test
  public void testValidate_withMissingRewardTransaction_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.remove(4);
    
    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "sum must equal"));
  }
  
  @Test
  public void testValidate_withTooManyRewardTransactions_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.get(0).getOutputs().get(0).setAmount(new BigDecimal("88"));
    transactions.add(TestDataFactory.createRewardTransaction(false));
    
    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "only be one Reward Transaction"));
  }
  
  @Test
  public void testValidate_withDoubleSpendInOneTransaction_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.get(0).getInputs().get(0).setPublicKey("someone2");
    
    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "transaction-input public-key must be unique for all transactions"));
  }
  
  @Test
  public void testValidate_withDoubleSpendInTwoTransactions_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.get(1).getInputs().get(0).setPublicKey("someone2");
    
    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "transaction-input public-key must be unique for all transactions"));
  }
  
  @Test
  public void testValidate_withDoubleSpendInThreeTransactions_shouldValidateFalse() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    transactions.get(1).getInputs().get(0).setPublicKey("someone2");
    transactions.get(4).getInputs().get(0).setPublicKey("someone2");
    
    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils
        .containsIgnoreCase(result.getReasonOfFailure(), "transaction-input public-key must be unique for all transactions"));
  }
    
  @Test
  public void testValidate_withDoubleSpendInOnlyMiningTransaction_shouldValidateTrueCauseItsHandledInRewardValidator() {
    List<Transaction> transactions = createBlockTransactions(false, false);
    
    TransactionInput input4 = new TransactionInput();
    input4.setAmount(new BigDecimal("100"));
    input4.setPublicKey("very-secret2");
    input4.setSequenceNumber(2l);
    input4.setSignature("x1x1x1");
    input4.setTimestamp(new Date().getTime() - 100l);
    
    transactions.get(4).getInputs().add(input4);
    transactions.get(4).getOutputs().add(input4);
    
    Assessment result = validator.validate(transactions);
    
    assertNotNull("verify result asessment is not null", result);
    assertEquals("verify that validation resulted correct", true, result.isValid());
  }
}
