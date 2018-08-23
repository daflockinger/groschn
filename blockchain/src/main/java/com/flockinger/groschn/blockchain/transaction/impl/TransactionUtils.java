package com.flockinger.groschn.blockchain.transaction.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import org.apache.commons.lang3.StringUtils;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;

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
  
  public boolean containsPubKeyOutput(Transaction transaction) {
    return emptyIfNull(transaction.getOutputs()).stream()
        .anyMatch(output -> StringUtils.equals(output.getPublicKey(), key));
  }
  
  public boolean containsPubKeyInput(Transaction transaction) {
    return emptyIfNull(transaction.getInputs()).stream()
        .anyMatch(output -> StringUtils.equals(output.getPublicKey(), key));
  }
  
  public Transaction getLatestTransaction(Transaction firstTx, Transaction secondTx) {
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
