package com.flockinger.groschn.blockchain.transaction;

import java.math.BigDecimal;
import com.flockinger.groschn.blockchain.model.Transaction;

public interface Bookkeeper {

  /**
   * Calculates the current block reward.
   * 
   * @param position
   * @return
   */
  BigDecimal calculateBlockReward(Long position);


  /**
   * Calculates the amount of change in Groschn, that the <br>
   * miner gets as reward of adding the Transaction to the block.
   * 
   * @param transaction
   * @return amount of change in Groschn
   */
  BigDecimal countChange(Transaction transaction);
}
