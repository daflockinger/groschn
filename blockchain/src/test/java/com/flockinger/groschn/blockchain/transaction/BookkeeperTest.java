package com.flockinger.groschn.blockchain.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.exception.validation.transaction.CheapskateException;
import com.flockinger.groschn.blockchain.exception.validation.transaction.NegativeTransactionBalanceException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.impl.BookkeeperImpl;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BookkeeperImpl.class})
public class BookkeeperTest {

  @Autowired
  private Bookkeeper keeper;
  
  private final static Long ONE_MILLION = 1000000l;
  
  @Test 
  public void testCalculateBlockReward_withUnderMillionAndOneRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 100, blockReward.longValueExact());
  }
  
  @Test 
  public void testCalculateBlockReward_withUnderThreeMillionRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(1 + ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 50, blockReward.longValueExact());
  }
  
  @Test 
  public void testCalculateBlockReward_withUnderSevenMillionRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(1 + 3*ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 25, blockReward.longValueExact());
  }
  
  @Test 
  public void testCalculateBlockReward_withUnderFifteenMillionRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(1 + 7*ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 12.5d, blockReward.doubleValue(),0.00000);
  }
  
  @Test 
  public void testCalculateBlockReward_withUnderThirtyOneMillionRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(1 + 15*ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 6.25d, blockReward.doubleValue(),0.00000);
  }
  
  @Test 
  public void testCalculateBlockReward_withUnderSixtyThreeMillionRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(1 + 31*ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 3.125d, blockReward.doubleValue(),0.00000);
  }
  
  @Test 
  public void testCalculateBlockReward_withUnderNinetyFiveRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(1 + 63*ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 1.5625, blockReward.doubleValue(),0.00000);
  }
  
  @Test 
  public void testCalculateBlockReward_withOverNinetyFiveRange_shouldReturnCorrect() {
    BigDecimal blockReward = keeper.calculateBlockReward(1 + 95*ONE_MILLION);
    assertNotNull("verify block reward is not null", blockReward);
    assertEquals("verify block reward is correct", 0, blockReward.longValue());
  }
  
  /**
   * Potentially slow test cause it's calculating 100 million times the reward!
   */
  @Test 
  public void testCalculateBlockReward_withTryToGetAllTheGroschnThereIs_shouldBeExactMaxAmount() {
    BigDecimal iWantItAll = new BigDecimal(0);
    
    for(long fakeBlockCount=1; fakeBlockCount <= (100 * ONE_MILLION); fakeBlockCount++) {
      iWantItAll = iWantItAll.add(keeper.calculateBlockReward(fakeBlockCount));
    }
    assertNotNull("verify block rewards are not null", iWantItAll);
    assertEquals("verify total block reward is exactly max amount of Groschn ever existing", 
        Block.MAX_AMOUNT_MINED_GROSCHN.longValue(), iWantItAll.longValueExact());
  }
  
  
  @Test
  public void testCountChange_withCorrectAmountsSomeNullsSomeChange_shouldCalculateCorrectly() {
    Transaction transaction = new Transaction();
    transaction.setInputs(ImmutableList.of(
        TestDataFactory.fakeInput("12.47300"),
        new TransactionInput(),
        TestDataFactory.fakeInput("2"),
        new TransactionInput(),
        TestDataFactory.fakeInput("0.12"),
        TestDataFactory.fakeInput("144")
        ));
    transaction.setOutputs(ImmutableList.of(
        TestDataFactory.fakeOutput("100.001"),
        TestDataFactory.fakeOutput("9.40900"),
        new TransactionOutput(),
        TestDataFactory.fakeOutput("0.591"),
        TestDataFactory.fakeOutput("48")
        ));
    BigDecimal change = keeper.countChange(transaction);
    
    assertNotNull("verify that calculated change is not null", change);
    assertTrue("verify that calculated change is correct", change.compareTo(new BigDecimal("0.592")) == 0);
  }
  
  @Test(expected=CheapskateException.class)
  public void testCountChange_withCorrectAmountsButNoChange_shouldThrowException() {
    Transaction transaction = new Transaction();
    transaction.setInputs(ImmutableList.of(
        TestDataFactory.fakeInput("12.47300"),
        new TransactionInput(),
        TestDataFactory.fakeInput("2"),
        new TransactionInput(),
        TestDataFactory.fakeInput("0.12"),
        TestDataFactory.fakeInput("144")
        ));
    transaction.setOutputs(ImmutableList.of(
        TestDataFactory.fakeOutput("100.000"),
        TestDataFactory.fakeOutput("9.40900"),
        new TransactionOutput(),
        TestDataFactory.fakeOutput("0.591"),
        TestDataFactory.fakeOutput("48.593")
        ));
    keeper.countChange(transaction);
  }
  
  @Test(expected=NegativeTransactionBalanceException.class)
  public void testCountChange_withNegativeBalance_shouldThrowException() {
    Transaction transaction = new Transaction();
    transaction.setInputs(ImmutableList.of(
        TestDataFactory.fakeInput("12.47300"),
        new TransactionInput(),
        TestDataFactory.fakeInput("2"),
        new TransactionInput(),
        TestDataFactory.fakeInput("0.12"),
        TestDataFactory.fakeInput("144")
        ));
    transaction.setOutputs(ImmutableList.of(
        TestDataFactory.fakeOutput("100.000"),
        TestDataFactory.fakeOutput("9.40900"),
        new TransactionOutput(),
        TestDataFactory.fakeOutput("0.591"),
        TestDataFactory.fakeOutput("48.693")
        ));
    keeper.countChange(transaction);
  }
}
