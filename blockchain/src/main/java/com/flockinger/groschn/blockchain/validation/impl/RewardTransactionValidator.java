package com.flockinger.groschn.blockchain.validation.impl;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.google.common.collect.ImmutableList;

@Component("RewardTransaction_Validator")
public class RewardTransactionValidator extends TransactionValidator {
  /*
   !! if there's a miners transaction having either exactly the reward amounts in- or output
   then the transaction will validate false !!
   * */
  @Autowired
  private BlockStorageService blockService;
  @Autowired
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
      var rewardAmount = bookKeeper.calculateBlockReward(blockService.getLatestBlock().getPosition());
      var minerPublicKey = findMinerPublicKey(value, rewardAmount);
      
      // extract reward transaction Input from the others
      var extract = extractRewardStatement(RewardContext.build(value.getInputs())
          .use(minerPublicKey, rewardAmount));
      //2. regular input funds must be equal to their current balance
      extract.get(NORMAL).forEach(super::isInputFundSufficient);
      //3. check if reward input exists
      verifyRewardInput(extract);
      //4. check that there's not more than one other input from the miner
      long normalMinerInputs = verifyPossibleOtherMinerInput(extract, minerPublicKey.get());
      //5. verify if reward outputs exist
      checkForRewardOutputs(RewardContext.build(value.getOutputs())
          .use(minerPublicKey, rewardAmount), normalMinerInputs);
      
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
    return context.getStatements().size() == 1 
      && context.getReward().compareTo(statement.getAmount()) == 0 
      && context.getMinerPublicKey().isPresent() 
      && context.getMinerPublicKey().get().equals(statement.getPublicKey());
  }
  
  /**
   * Only the miners pubKey has an exact reward in and output 
   * and also has an equal in and output sum or an higher output sum
   * 
   * @param transaction
   * @param reward
   * @return
   */
  private Optional<String> findMinerPublicKey(Transaction transaction, BigDecimal reward) {
    List<String> possibleMinersFromInput = transaction.getInputs().stream()
        .filter(input -> input.getAmount().compareTo(reward) == 0).map(TransactionInput::getPublicKey)
        .collect(Collectors.toList());
    List<String> possibleMinersFromOutput = transaction.getOutputs().stream()
        .filter(output -> output.getAmount().compareTo(reward) == 0).map(TransactionOutput::getPublicKey)
        .collect(Collectors.toList());
    possibleMinersFromInput.retainAll(possibleMinersFromOutput);    
    return possibleMinersFromInput.stream()
      .filter(key -> calcualteTransactionBalance(transaction, statement -> statement.getPublicKey().equals(key)) <= 0)
      .findFirst();
  }  
  
  private void verifyRewardInput(Map<Boolean, List<TransactionInput>> extract) {
    verifyAssessment(extract.containsKey(REWARD) && 
        extract.get(REWARD).size() == 1, "There must be exactly one miner's Reward input!");
  }
  
  private long verifyPossibleOtherMinerInput(Map<Boolean, List<TransactionInput>> extract, String minerPublicKey) {
    long normalMinerInputCount = extract.get(NORMAL).stream()
        .filter(input -> input.getPublicKey().equals(minerPublicKey)).count();
    verifyAssessment(normalMinerInputCount <= 1, 
        "Besides the reward Transaction input the miner can have only ONE other!");
    return normalMinerInputCount;
  }
  
  private void checkForRewardOutputs(RewardContext<TransactionOutput> context, long normalMinerInputs) {
    List<TransactionOutput> minerOutputs = context.getStatements().stream()
        .filter(output -> output.getPublicKey().equals(context.getMinerPublicKey().orElse("")))
        .collect(Collectors.toList());
    boolean containsExactlyOneReward = minerOutputs.stream()
        .filter(output -> output.getAmount().compareTo(context.getReward()) == 0).count() == 1;
    boolean containsAlsoTheChange = minerOutputs.size() == 2 + normalMinerInputs;
    verifyAssessment(containsExactlyOneReward && containsAlsoTheChange, 
        "A reward transaction must contain one Reward Transaction Output and another one for the Change!");
  }
  
  /* 
   * reward has a higher output sum than input sum
   */
  @Override
  protected void verifyTransactionBalance(Transaction transaction) {
    verifyAssessment(calcualteTransactionBalance(transaction) <= 0, 
        "The Reward's total input amount must be higher than  it's total output amount!");
  }
  
  /*
   * Skip checking for sufficient funds in the regular run, since the 
   * reward input has no funding. Will be checked separately.
   * */
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
