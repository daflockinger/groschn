package com.flockinger.groschn.blockchain.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class TransactionOutput implements Serializable, Sequential {
  /**
  * 
  */
  private static final long serialVersionUID = 4422348192122659030L;

  private BigDecimal amount = null;

  private String publicKey = null;

  private Long timestamp = null;

  private Long sequenceNumber = null;

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public Long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  @Override
  public String toString() {
    return "TransactionOutput [amount=" + amount + ", publicKey=" + publicKey + ", timestamp="
        + timestamp + ", sequenceNumber=" + sequenceNumber + "]";
  }
}
