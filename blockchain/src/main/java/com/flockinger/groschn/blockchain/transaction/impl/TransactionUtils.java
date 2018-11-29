package com.flockinger.groschn.blockchain.transaction.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class TransactionUtils {

  private String key;
  
  public static TransactionUtils build(String publicKey) {
    TransactionUtils utils = new TransactionUtils();
    utils.setKey(publicKey);
    return utils;
  }

  public void setKey(String key) {
    this.key = key;
  }
  
  
  public Optional<Transaction> findLatestExpenseTransaction(List<Transaction> transactions) {
    return transactions.stream()
        .filter(this::containsPubKeyInput)
        .reduce(this::getLatestTransaction);
  }
  
  private boolean containsPubKeyInput(Transaction transaction) {
    return emptyIfNull(transaction.getInputs()).stream()
        .anyMatch(input -> StringUtils.equals(input.getPublicKey(), key));
  }
  
  private Transaction getLatestTransaction(Transaction firstTx, Transaction secondTx) {
    return (latestTxInputTimeStamp(firstTx, key) > latestTxInputTimeStamp(secondTx, key)) 
        ? firstTx : secondTx;
  }
  
  private Long latestTxInputTimeStamp(Transaction transaction, String publicKey) {
    return emptyIfNull(transaction.getInputs()).stream()
      .filter(input -> StringUtils.equals(input.getPublicKey(),publicKey))
      .map(TransactionInput::getTimestamp)
      .reduce((firstStamp, secondStamp) -> (firstStamp > secondStamp)
          ? firstStamp : secondStamp).orElse(0l);
  }
}
