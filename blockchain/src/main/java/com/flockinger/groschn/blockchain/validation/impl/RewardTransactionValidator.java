package com.flockinger.groschn.blockchain.validation.impl;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.google.common.collect.ImmutableList;

/**
 * @author root
 *
 */
public class RewardTransactionValidator extends TransactionValidator {

  
  /*
   
    difference between reward and normal transaction:
   - reward has 2 inputs of the same pubKey
   + reward has a higher output sum than input sum
   - reward contains one extra reward-in and output and one change-output
   
   * */
  private BlockStorageService blockService;
  private Bookkeeper bookKeeper;
  
  private final static Boolean NORMAL = false;
  private final static Boolean REWARD = true;
  
  
  @Override
  public Assessment validate(Transaction value) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new Transaction:
    try {
      //1. verify regular Transaction stuff      
      var txAssessment = super.validate(value);
      verifyAssessment(txAssessment.isValid(), txAssessment.getReasonOfFailure());
      // get Miner's public key and calculate it's correct Reward amount.
      var minerPublicKey = findMinerPublicKey(value.getInputs());
      var rewardAmount = bookKeeper.calculateBlockReward(blockService.getLatestBlock().getPosition());
      
      // extract reward transaction Input from the others
      var extract = extractRewardStatement(RewardContext.build(value.getInputs())
          .use(minerPublicKey, rewardAmount));
      //2. regular input funds must be equal to the current balance
      extract.get(NORMAL).forEach(super::isInputFundSufficient);
      //3. check if reward input exists
      verifyRewardInput(extract);
 
      //4. verify if reward outputs exist
      checkForRewardOutputs(RewardContext
          .build(value.getOutputs()).use(minerPublicKey, rewardAmount));
      
      isBlockValid.setValid(true);
    } catch (BlockchainException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }  
  return isBlockValid;
  }

  
  private Map<Boolean, List<TransactionInput>> extractRewardStatement(RewardContext<TransactionInput> context) {
    return context.getStatements().stream().map(statement -> RewardContext.build(statement).use(context))
        .collect(groupingBy(this::isRewardTransactionStatement, 
            mapping(RewardContext::getStatement, toList())));
  }
  
  private <T extends TransactionOutput> boolean isRewardTransactionStatement(RewardContext<T> context) {
    T statement = context.getStatement();
    if(statement == null) {
      return false;
    }
    return context.getReward().compareTo(statement.getAmount()) == 0 
     && context.getMinerPublicKey().isPresent() 
     && context.getMinerPublicKey().get().equals(statement.getPublicKey());
  }
  
  private Optional<String> findMinerPublicKey(List<TransactionInput> inputs) {
    return inputs.stream()
        .collect(groupingBy(TransactionInput::getPublicKey,counting()))
        .entrySet().stream()
        .filter(pubKeyOccurence -> pubKeyOccurence.getValue() == 2)
        .map(Entry::getKey).findFirst();
  }
  
  private void verifyRewardInput(Map<Boolean, List<TransactionInput>> extract) {
    verifyAssessment(extract.get(REWARD).size() == 1, "There must be exactly one miner's Reward input!");
  }
  
  private void checkForRewardOutputs(RewardContext<TransactionOutput> context) {
    //TODO implement
  }
  
  
  /* 
   * reward has a higher output sum than input sum
   */
  @Override
  protected void verifyTransactionBalance(Transaction transaction) {
    var inputAmount = transaction.getInputs().stream().map(TransactionInput::getAmount)
      .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    var outputAmount = transaction.getOutputs().stream().map(TransactionOutput::getAmount)
        .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    verifyAssessment(inputAmount.compareTo(outputAmount) < 0, 
        "The Reward's total input amount must be higher than  it's total output amount!");
  }
  
  @Override
  protected void isInputFundSufficient(TransactionInput input) {}
  
  
  
  private final static class RewardContext<T extends TransactionOutput> {
    private List<T> statements;
    private Optional<String> minerPublicKey;
    private BigDecimal reward;
    public static <T extends TransactionOutput> RewardContext<T> build(T statement) {
      return build(ImmutableList.of(statement));
    }
    public static <T extends TransactionOutput> RewardContext<T> build(List<T> statements) {
      RewardContext<T> context = new RewardContext<T>();
      context.setStatements(statements);
      return context;
    }
    public RewardContext<T> use(RewardContext<T> context) {
      return use(context.getMinerPublicKey(), context.getReward());
    }
    public RewardContext<T> use(Optional<String> minerPublicKey, BigDecimal reward) {
      this.minerPublicKey = minerPublicKey;
      this.reward = reward;
      return this;
    }
    public List<T> getStatements() {
      return statements;
    }
    public T getStatement() {
      return statements.stream().findFirst().orElse(null);
    }
    private void setStatements(List<T> statements) {
      this.statements = statements;
    }
    public Optional<String> getMinerPublicKey() {
      return minerPublicKey;
    }
    public BigDecimal getReward() {
      return reward;
    }
  }
}
