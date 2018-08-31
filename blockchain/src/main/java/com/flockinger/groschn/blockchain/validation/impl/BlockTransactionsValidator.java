package com.flockinger.groschn.blockchain.validation.impl;

import java.util.List;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;

@Component
public class BlockTransactionsValidator implements Validator<List<Transaction>>{
/*
   Probably need multiple kinds of transaction Validators one for:
   + When validating for Block -> BlockTransactionsValidator-> check for Reward and perfect balance
   + When validating for fetching Transactions from pool -> TransactionValidator -> for single Transactions
   
   TODO maybe create a Uber-Interface returning only the valid Transactions
   
   Overall checks (regarding all Transactions):
   ********************************************
   - verify that a input publicKey is unique for all
     transactions in one block (so there cannot be multiple transactions
     containing the same input public-key!!! -> otherwise the balance calculation can fail, 
     and or the the whole balance in either one of the transactions can be tampered with -> possible double spend)
   - verify correct double-bookkeeping
     - total transactions input amounts must equal output amounts (with having the reward Transaction)

   Only special check the Reward Transaction:
   ********************************
   !! FIXME the reward transaction can be found when searching for the only one 
     that has a higher out value than in value!!
   - verify correct reward input amount
   - must be signed by the miner
   - verify correct reward output
   - verify correct change output
   - only the reward transaction has a higher output value than input value
     - TODO ideally treat the reward transaction differently
   
   * */
  
  @Override
  public Assessment validate(List<Transaction> transactions) {
    // TODO Auto-generated method stub
    return null;
  }

}
