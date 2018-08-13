package com.flockinger.groschn.blockchain.repository.model;

public class StoredTransactionPointCut {
  
  private String transactionId;
  private String transactionHash;
  
  public String getTransactionHash() {
    return transactionHash;
  }
  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }
  public String getTransactionId() {
    return transactionId;
  }
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }
}
