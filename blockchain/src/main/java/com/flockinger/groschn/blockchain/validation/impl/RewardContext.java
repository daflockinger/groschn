package com.flockinger.groschn.blockchain.validation.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.google.common.collect.ImmutableList;

class RewardContext<T> {
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

  public Predicate<TransactionOutput> hasPublicKey() {
    return output -> minerPublicKey.isPresent()
        && output.getPublicKey().equals(minerPublicKey.get());
  }
  
  public Predicate<TransactionInput> containsNoPublicKey() {
    return input -> minerPublicKey.isPresent()
        && !input.getPublicKey().equals(minerPublicKey.get());
  }

  public BigDecimal getReward() {
    return reward;
  }

  public Predicate<TransactionOutput> isAmountEqualToReward() {
    return output -> reward.compareTo(output.getAmount()) == 0;
  }
}
