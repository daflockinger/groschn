package com.flockinger.groschn.blockchain.validation.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;
import com.google.common.collect.ImmutableList;

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
  
  @Autowired
  private TransactionValidator transactionValidator;
  @Autowired
  private HashGenerator hasher;
  @Autowired
  @Qualifier("ECDSA_Signer")
  private Signer signer;
  
  @Override
  public Assessment validate(List<Transaction> transactions) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new BlockTransactions:
    try {
      //1. verify correct double-bookkeeping (input amounts must equal output amounts)
      verifyEqualBalance(transactions);     
      //2. extract reward transaction
      Map<Boolean, List<Transaction>> extract = extractRewardTransaction(transactions);
      List<Transaction> normalTransactions = extract.get(false);
      //3. verify that a input publicKey transaction-unique in one block 
      verifyTransactionUniqueInputPublicKey(normalTransactions);
      //4. verify reward transaction
      verifyReward(extract.get(true).get(0));
      //5. verify normal transactions
      for(Transaction normalTransaction: normalTransactions) {
        transactionValidator.validate(normalTransaction);
      }
      isBlockValid.setValid(true);
    } catch (BlockchainException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }  
  return isBlockValid;
  }
  
  private void verifyTransactionUniqueInputPublicKey(List<Transaction> transactions) {
    Map<String, Long> groupedTransactionPubKeys = transactions.stream()
        .map(Transaction::getInputs)
        .flatMap(Collection::stream)
        .collect(Collectors.groupingBy(TransactionInput::getPublicKey, Collectors.counting()));
    boolean areKeysUnique = groupedTransactionPubKeys.values().stream().allMatch(pubKeyCount -> pubKeyCount == 1);
    verifyAssessment(areKeysUnique, "Each transaction-input public-key must be unique for all transactions in one Block!");
  }
  
  private void verifyEqualBalance(List<Transaction> transactions) {
    verifyAssessment(compareTransactionInputsWithOutputs(transactions) == 0, 
        "Block transactions output total sum must equal to inputs total sum!");
  }
  
  private int compareTransactionInputsWithOutputs(List<Transaction> transactions) {
    BigDecimal outputSum = transactions.stream()
        .map(Transaction::getOutputs)
        .flatMap(Collection::stream)
        .map(TransactionOutput::getAmount)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
      BigDecimal inputSum = transactions.stream()
          .map(Transaction::getInputs)
          .flatMap(Collection::stream)
          .map(TransactionInput::getAmount)
          .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
      return inputSum.compareTo(outputSum);
  }

  
  private Map<Boolean, List<Transaction>> extractRewardTransaction(List<Transaction> transactions) {
    Map<Boolean, List<Transaction>> extract = transactions.stream()
        .collect(Collectors.groupingBy(this::isOutputSumHigherThanInputSum));
    if(extract.get(true).size() != 1) {
      throw new AssessmentFailedException("There can only be one Reward Transaction no more or less!");
    }
    return extract;
  }
  
  private Boolean isOutputSumHigherThanInputSum(Transaction transaction) {
    int inputSumComaredToOutputSum = compareTransactionInputsWithOutputs(ImmutableList.of(transaction));
    return inputSumComaredToOutputSum < 0;
  }
  
  
  /*
   
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
   
   difference between reward and normal transaction:
   - reward has 2 inputs of the same pubKey
   - reward has a higher output sum than input sum
   - reward contains one extra reward-in and output and one change-output
   
   * */
  private void verifyReward(Transaction reward) {
    
  }
  
  
  
  
  private void verifyAssessment(boolean isValid, String errorMessage) {
    if(!isValid) {
      throw new AssessmentFailedException(errorMessage);
    }
  }
}
