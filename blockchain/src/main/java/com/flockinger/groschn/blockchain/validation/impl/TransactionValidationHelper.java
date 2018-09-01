package com.flockinger.groschn.blockchain.validation.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.google.common.collect.ImmutableList;

@Component
public class TransactionValidationHelper {
  
  
  public int calcualteTransactionBalance(List<Transaction> transactions, Predicate<TransactionOutput> filter) {
    var inputAmount = transactions.stream().map(Transaction::getInputs).flatMap(Collection::stream)
       .filter(filter).map(TransactionInput::getAmount)
       .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    var outputAmount = transactions.stream().map(Transaction::getOutputs).flatMap(Collection::stream)
       .filter(filter).map(TransactionOutput::getAmount)
       .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    return inputAmount.compareTo(outputAmount);
  }
  
  public int calcualteTransactionBalance(Transaction transaction, Predicate<TransactionOutput> filter) {
    return calcualteTransactionBalance(ImmutableList.of(transaction), filter);
  }
  
  public int calcualteTransactionBalance(Transaction transaction) {
    return calcualteTransactionBalance(transaction, statement -> true);
  }
  
  public List<String> findMinerPublicKeys(Transaction transaction, BigDecimal reward) {
    List<String> inputMiners = getPossibleMinersFrom(transaction.getInputs(), reward);
    List<String> outputMiners = getPossibleMinersFrom(transaction.getOutputs(), reward);
    inputMiners.retainAll(outputMiners);
    return inputMiners.stream().filter(key -> calcualteTransactionBalance(transaction,
        statement -> statement.getPublicKey().equals(key)) <= 0).collect(Collectors.toList());
  }
  
  private <T extends TransactionOutput> List<String> getPossibleMinersFrom(List<T> statements, BigDecimal reward) {
    return statements.stream().filter(input -> input.getAmount().compareTo(reward) == 0)
        .map(TransactionOutput::getPublicKey).collect(Collectors.toList());
  }
  
}
