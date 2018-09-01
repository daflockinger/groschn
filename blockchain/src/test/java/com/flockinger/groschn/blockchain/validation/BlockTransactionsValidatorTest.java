package com.flockinger.groschn.blockchain.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.validation.impl.BlockTransactionsValidator;
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidationHelper;


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
    transactions.get(0).getOutputs().get(2).setAmount(new BigDecimal("289"));

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
  
  
  /*
   
  @Test
  public void testValidate_with_should() {
    
  }
   
   * */
  
  
  private List<Transaction> createBlockTransactions(boolean rewardRewardStatementsOnly, boolean rewardTxOnly) {
    List<Transaction> transactions = new ArrayList<>();
    
    if(!rewardTxOnly) {
      transactions.add(TestDataFactory.createValidTransaction("someone1", "someone2", "someone3", "great1"));
      transactions.add(TestDataFactory.createValidTransaction("great1", "someone4", "someone5", "great1"));
      transactions.add(TestDataFactory.createValidTransaction("someone6", "someone7", "anotherone3", "someone4"));
      transactions.add(TestDataFactory.createValidTransaction("anotherone2", "anotherone4", "anotherone1", "someone7"));
    }
    transactions.add(TestDataFactory.createRewardTransaction(rewardRewardStatementsOnly));
    if(!rewardTxOnly) {
      transactions.add(TestDataFactory.createValidTransaction("some1", "some2", "some3", "great21"));
      transactions.add(TestDataFactory.createValidTransaction("great21", "some4", "some5", "great21"));
      transactions.add(TestDataFactory.createValidTransaction("some6", "some7", "another3", "some4"));
      transactions.add(TestDataFactory.createValidTransaction("another2", "another4", "another1", "some7"));
      transactions.add(TestDataFactory.createValidTransaction("great31", "someone24", "someone25", "great31"));
      transactions.add(TestDataFactory.createValidTransaction("some26", "someone27", "anotherone23", "someone24"));
      transactions.add(TestDataFactory.createValidTransaction("anotherone22", "anotherone24", "anotherone21", "someone25"));
    }
    return transactions;
  }
  
  
}
