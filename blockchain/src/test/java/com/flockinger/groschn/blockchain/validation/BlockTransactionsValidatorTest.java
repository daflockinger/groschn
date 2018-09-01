package com.flockinger.groschn.blockchain.validation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
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
  public void test_with_should() {
    
  }
  
  
  
  
}
