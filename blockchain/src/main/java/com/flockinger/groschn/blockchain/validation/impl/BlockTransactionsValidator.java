package com.flockinger.groschn.blockchain.validation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
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
   1. verify that a input publicKey is unique for all
     transactions in one block (so there cannot be multiple transactions
     containing the same input public-key!!! -> otherwise the balance calculation can fail, 
     and or the the whole balance in either one of the transactions can be tampered with -> possible double spend)
   2. verify correct double-bookkeeping
     - total transactions input amounts must equal output amounts (with having the reward Transaction)
       
   * */
  
  @Autowired
  @Qualifier("Transaction_Validator")
  private Validator<Transaction> transactionValidator;
  @Autowired
  @Qualifier("RewardTransaction_Validator")
  private Validator<Transaction> rewardTransactionValidator;
  @Autowired
  private TransactionValidationHelper helper;
  
  private final static Boolean NORMAL = false;
  private final static Boolean REWARD = true;
  
  @Override
  public Assessment validate(List<Transaction> transactions) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new BlockTransactions:
    try {
      //1. verify correct double-bookkeeping (input amounts must equal output amounts)
      verifyEqualBalance(transactions);     
      //2. extract reward transaction
      Map<Boolean, List<Transaction>> extract = extractRewardTransaction(transactions);
      //3. verify that a input publicKey transaction-unique in one block 
      checkDoubleSpendInputs(extract);
      //4. verify reward transaction
      rewardTransactionValidator.validate(extract.get(REWARD).get(0));
      //5. verify normal transactions
      for(Transaction normalTransaction: extract.get(NORMAL)) {
        transactionValidator.validate(normalTransaction);
      }
      isBlockValid.setValid(true);
    } catch (BlockchainException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }  
  return isBlockValid;
  }
  
  /**
   * Very important check that Transaction-Inputs are unique on the publicKey Level.
   * There's no internal verification for the Reward-Transaction since this is already done
   * in it's Validator.
   * 
   * @param extract
   */
  private void checkDoubleSpendInputs(Map<Boolean, List<Transaction>> extract) {
    List<String> inputKeys = new ArrayList<String>();
    inputKeys.addAll(extract.get(NORMAL).stream()
        .map(Transaction::getInputs).flatMap(Collection::stream)
        .map(TransactionInput::getPublicKey).collect(Collectors.toList()));
    // for the reward-transaction duplicity is transaction-internally already checked,
    // so only the external duplicity (only each unique reward-input-key) is needed for external checking.
    inputKeys.addAll(extract.get(REWARD).stream() 
        .map(Transaction::getInputs).flatMap(Collection::stream)
        .map(TransactionInput::getPublicKey).collect(Collectors.toSet()));
    Map<String, Long> groupedNormalTransactionPubKeys = inputKeys.stream()
        .collect(Collectors.groupingBy(key -> key, Collectors.counting()));
    boolean areKeysUnique = groupedNormalTransactionPubKeys.values().stream().allMatch(pubKeyCount -> pubKeyCount == 1);
    verifyAssessment(areKeysUnique, "Each transaction-input public-key must be unique for all transactions in one Block!");
  }
  
  private void verifyEqualBalance(List<Transaction> transactions) {
    verifyAssessment(helper.calcualteTransactionBalance(transactions, tx -> true) == 0, 
        "Block transactions output total sum must equal to inputs total sum!");
  }
  
  private Map<Boolean, List<Transaction>> extractRewardTransaction(List<Transaction> transactions) {
    Map<Boolean, List<Transaction>> extract = transactions.stream()
        .collect(Collectors.groupingBy(this::isOutputSumHigherorEqualThanInputSum));
    if(extract.get(true).size() != 1) {
      throw new AssessmentFailedException("There can only be one Reward Transaction no more or less!");
    }
    return extract;
  }
  
  private Boolean isOutputSumHigherorEqualThanInputSum(Transaction transaction) {
    return helper.calcualteTransactionBalance(transaction) <= 0;
  }
  
  private void verifyAssessment(boolean isValid, String errorMessage) {
    if(!isValid) {
      throw new AssessmentFailedException(errorMessage);
    }
  }
}
