package com.flockinger.groschn.blockchain.transaction.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.exception.validation.transaction.CheapskateException;
import com.flockinger.groschn.blockchain.exception.validation.transaction.NegativeTransactionBalanceException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;

@Component
public class BookkeeperImpl implements Bookkeeper {

  private final static Long ONE_MILLION = 1000000l;
  
  @Override
  public BigDecimal calculateBlockReward(Long position) {
    BigDecimal reward = new BigDecimal(0);
    /* from             to   block     reward [Groschn]     blocks         total rewards [Groschn]
     *          1   -    1.000.000       100,0000           1.000.000      100.000.000
     *  1.000.001   -    3.000.000        50,0000           2.000.000      100.000.000
     *  3.000.001   -    7.000.000        25,0000           4.000.000      100.000.000
     *  7.000.001   -   15.000.000        12,5000           8.000.000      100.000.000
     * 15.000.001   -   31.000.000         6,2500          16.000.000      100.000.000
     * 31.000.001   -   63.000.000         3,1250          32.000.000      100.000.000
     * 63.000.001   -   95.000.000         1,5625          32.000.000       50.000.000
     * 95.000.001   -   ...                0.0000
     *                                   Total sum of Groschn ever mined:  650.000.000
     * */
    if(position <= ONE_MILLION) {
      reward = new BigDecimal("100");
    } else if (position <= 3*ONE_MILLION) {
      reward = new BigDecimal("50");
    } else if (position <= 7*ONE_MILLION) {
      reward = new BigDecimal("25");
    } else if (position <= 15*ONE_MILLION) {
      reward = new BigDecimal("12.5");
    } else if (position <= 31*ONE_MILLION) {
      reward = new BigDecimal("6.25");
    } else if (position <= 63*ONE_MILLION) {
      reward = new BigDecimal("3.125");
    } else if (position <= 95*ONE_MILLION) {
      reward = new BigDecimal("1.5625");
    }
    return reward;
  }

  
  @Override
  public BigDecimal countChange(Transaction transaction) {
    BigDecimal inputSum = emptyIfNull(transaction.getInputs()).stream()
      .map(TransactionInput::getAmount)
      .filter(Objects::nonNull)
      .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    BigDecimal outputSum = emptyIfNull(transaction.getOutputs()).stream()
      .map(TransactionOutput::getAmount)
      .filter(Objects::nonNull)
      .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    
    BigDecimal change = inputSum.subtract(outputSum);
    
    if(change.compareTo(BigDecimal.ZERO) == 0) {
      throw new CheapskateException("You should at least give a little tip, you cheap bastard!");
    } else if(change.compareTo(BigDecimal.ZERO) < 0) {
      throw new NegativeTransactionBalanceException("You can't spend more than what you had initially!");
    }
    return change;
  }
}
