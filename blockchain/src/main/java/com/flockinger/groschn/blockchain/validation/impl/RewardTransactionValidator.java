package com.flockinger.groschn.blockchain.validation.impl;

import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.commons.exception.BlockchainException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("RewardTransaction_Validator")
class RewardTransactionValidator extends TransactionValidator {
  
  @Autowired
  private Bookkeeper bookKeeper;

  @Override
  public Assessment validate(Transaction value) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new Transaction:
    try {
      // 1. verify regular Transaction stuff
      var txAssessment = super.validate(value);
      verifyAssessment(txAssessment.isValid(), txAssessment.getReasonOfFailure());
      // get Miner's public key and calculate it's correct Reward amount.
      var rewardAmount =
          bookKeeper.calculateCurrentBlockReward();
      // 2. find publicKey and verify that exactly one was found
      var minerPublicKey = findMinerPublicKey(value, rewardAmount);
      // extract reward transaction Input from the others
      var normalInputs = extractNormalInputs(
          RewardContext.build(value.getInputs()).use(minerPublicKey, rewardAmount));
      // 3. check if the transaction really only contains miner in- and outputs
      checkRewardTransactionOnlyContainsMinerInAndOutputs(value, minerPublicKey.orElse(""));
      // 4. regular input funds must be equal to their current balance
      normalInputs.forEach(super::isInputFundSufficient);
      // 5. check that there's not more than one other input from the miner
      long normalMinerInputs = verifyPossibleOtherMinerInput(normalInputs, minerPublicKey.get());
      // 6. verify if reward outputs exist
      checkForRewardOutputs(
          RewardContext.build(value.getOutputs()).use(minerPublicKey, rewardAmount),
          normalMinerInputs);
      // 7. verify that there are no Double Spend Inputs
      checkDoubleSpendInputs(RewardContext.build(normalInputs)
          .use(minerPublicKey,rewardAmount));
      isBlockValid.setValid(true);
    } catch (BlockchainException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }
    return isBlockValid;
  }

  
  /**
   * Only the miners pubKey has an exact reward in and output and also has an equal in and output
   * sum or an higher output sum
   * 
   * @param transaction
   * @param reward
   * @return
   */
  private Optional<String> findMinerPublicKey(Transaction transaction, BigDecimal reward) {
    var rewardKeys = helper.findMinerPublicKeys(transaction, reward);
    verifyRewardInputs(rewardKeys);
    return rewardKeys.stream().findFirst();
  }

  private void verifyRewardInputs(List<String> rewardPublicKeys) {
    verifyAssessment(rewardPublicKeys.size() == 1, "There must be exactly one miner's Reward input!");
  }

  private List<TransactionInput> extractNormalInputs(
      RewardContext<TransactionInput> context) {
    return context.getStatements().stream()
        .filter(input -> !isRewardTransactionStatement(RewardContext.build(input).use(context)))
        .collect(Collectors.toList());
  }

  private <T extends TransactionOutput> boolean isRewardTransactionStatement(
      RewardContext<T> context) {
    T statement = context.getStatement();
    return context.getStatements().size() == 1 && context.isAmountEqualToReward().test(statement)
        && context.hasPublicKey().test(statement);
  }


  private long verifyPossibleOtherMinerInput(List<TransactionInput> normalInputs,
      String minerPublicKey) {
    long normalMinerInputCount = normalInputs.stream()
        .filter(input -> input.getPublicKey().equals(minerPublicKey)).count();
    verifyAssessment(normalMinerInputCount <= 1,
        "Besides the reward Transaction input the miner can have only ONE other!");
    return normalMinerInputCount;
  }

  private void checkForRewardOutputs(RewardContext<TransactionOutput> context,
      long normalMinerInputs) {
    List<TransactionOutput> minerOutputs = context.getStatements().stream()
        .filter(context.hasPublicKey()).collect(Collectors.toList());
    boolean containsExactlyOneReward =
        minerOutputs.stream().filter(context.isAmountEqualToReward()).count() == 1;
    boolean containsAlsoTheChange = minerOutputs.size() == 2 + normalMinerInputs;
    verifyAssessment(containsExactlyOneReward && containsAlsoTheChange,
        "A reward transaction must contain one Reward Transaction Output and another one for the Change!");
  }
  
  
  /**
   * Very important check that verifies that all Transaction inputs except the miner's
   * have are unique on Transaction-Input PublicKey Level. This check verifies only the 
   * Transaction internal integrity.
   * 
   * @param context
   */
  private void checkDoubleSpendInputs(RewardContext<TransactionInput> context) {
    Map<String, Long> groupedTransactionPubKeys = context.getStatements().stream()
        .filter(context.containsNoPublicKey())
        .collect(Collectors.groupingBy(TransactionInput::getPublicKey, Collectors.counting()));  
    boolean isDoubleSpendFree = groupedTransactionPubKeys.values().stream().allMatch(pubKeyCount -> pubKeyCount == 1);
    verifyAssessment(isDoubleSpendFree, "Reward Transaction contains double spend inputs!!");
  }

  /*
   * reward has a higher output sum than input sum
   */
  @Override
  protected void verifyTransactionBalance(Transaction transaction) {
    verifyAssessment(helper.calcualteTransactionBalance(transaction) <= 0,
        "The Reward's total output amount must be higher or equal to it's total input amount!");
  }

  /*
   * Skip checking for sufficient funds in the regular run, since the reward input has no funding.
   * Will be checked separately.
   */
  @Override
  protected void isInputFundSufficient(TransactionInput input) {}
  
  
  /**
   * The reward transaction is usually auto generated by the block chain, so it only contains transaction
   * in and outputs from the miner and must not contain in/outputs from someone else.
   * 
   * @param transaction
   * @param minerPublicKey
   */
  private void checkRewardTransactionOnlyContainsMinerInAndOutputs(Transaction transaction, String minerPublicKey) {
    boolean onlyContainsMinerInputs = transaction.getInputs().stream()
        .allMatch(input -> StringUtils.equals(input.getPublicKey(),minerPublicKey));
    boolean onlyContainsMinerOutputs = transaction.getOutputs().stream()
        .allMatch(input -> StringUtils.equals(input.getPublicKey(),minerPublicKey));  
    verifyAssessment(onlyContainsMinerInputs && onlyContainsMinerOutputs, 
        "Reward transaction must not contain in/outputs other than from the miner!");
  }
}
