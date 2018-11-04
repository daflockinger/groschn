package com.flockinger.groschn.blockchain.validation.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;
import com.flockinger.groschn.commons.exception.BlockchainException;

@Component("BlockTransaction_Validator")
public class BlockTransactionsValidator implements Validator<List<Transaction>>{
  /*
   Overall checks (regarding all Transactions):
   ********************************************
   1. verify that a input publicKey is unique for all
     transactions in one block (so there cannot be multiple transactions
     containing the same input public-key!!! -> otherwise the balance calculation can fail, 
     and or the the whole balance in either one of the transactions can be tampered with -> possible double spend)
   2. verify correct double-bookkeeping
     - total transactions input amounts must equal output amounts (with having the reward Transaction)
   **/
  
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
   * Very important check that Transaction-Inputs are unique on the publicKey Level for one Block.
   * The only exception is the miner's publicKey which must be present, but can be listed 2 times
   * (once for the reward and the second time for the miner's balance) at most.
   * 
   * @param extract
   */
  private void checkDoubleSpendInputs(Map<Boolean, List<Transaction>> extract) {
    var normalTransactionKeys = extractInputKeys(extract.get(NORMAL));
    var minerKeys = extractInputKeys(extract.get(REWARD));
    var allKeys = new ArrayList<>(normalTransactionKeys);
    allKeys.addAll(minerKeys);
    var minerKey = minerKeys.stream().findFirst();
    long minerKeyOccurrences = allKeys.stream()
        .filter(key -> minerKey.isPresent())
        .filter(key -> StringUtils.equals(minerKey.get(), key))
        .count();
    boolean isMinerPubKeyNotDoubleSpent = minerKeyOccurrences  > 0 && minerKeyOccurrences <=2;
    verifyAssessment(areNormalTransactionInputPubKeysUniquene(normalTransactionKeys)
        && isMinerPubKeyNotDoubleSpent, "Each transaction-input public-key must be unique for all transactions in one Block!");
  }
  
  private List<String> extractInputKeys(List<Transaction> transactions) {
    return transactions.stream()
        .map(Transaction::getInputs).flatMap(Collection::stream)
        .map(TransactionInput::getPublicKey).collect(Collectors.toList());
  }
  
  private boolean areNormalTransactionInputPubKeysUniquene(List<String> normalTransactionKeys) {
    return normalTransactionKeys.stream()
        .collect(Collectors.groupingBy(key -> key, Collectors.counting()))
        .values().stream().allMatch(pubKeyCount -> pubKeyCount == 1);
  }
 
  
  private void verifyEqualBalance(List<Transaction> transactions) {
    verifyAssessment(helper.calcualteTransactionBalance(transactions, tx -> true) == 0, 
        "Block transactions output total sum must equal to inputs total sum!");
  }
  
  private Map<Boolean, List<Transaction>> extractRewardTransaction(List<Transaction> transactions) {
    Map<Boolean, List<Transaction>> extract = transactions.stream()
        .collect(Collectors.groupingBy(this::isOutputSumHigherorEqualThanInputSum));
    if(!extract.containsKey(NORMAL)) {
      extract.put(NORMAL, new ArrayList<>());
    }
    if(!extract.containsKey(REWARD) || extract.get(REWARD).size() != 1) {
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
