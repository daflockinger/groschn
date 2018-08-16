package com.flockinger.groschn.blockchain.model;

import java.io.Serializable;

public class TransactionPointCut implements Serializable {
  /**
  * 
  */
  private static final long serialVersionUID = 5298716270384578184L;

  private String transactionHash;
  private Long sequenceNumber;

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }

  public Long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }
}
