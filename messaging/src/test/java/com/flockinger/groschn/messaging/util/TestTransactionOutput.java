package com.flockinger.groschn.messaging.util;

import java.io.Serializable;
import java.math.BigDecimal;
import com.flockinger.groschn.blockchain.model.Sequential;

public class TestTransactionOutput implements Serializable, Sequential {
  /**
  * 
  */
  private static final long serialVersionUID = 4422348192122659030L;

  private BigDecimal amount;

  private String publicKey;

  private Long timestamp;

  private Long sequenceNumber;

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
    return "TestTransactionOutput [amount=" + amount + ", publicKey=" + publicKey + ", timestamp="
        + timestamp + ", sequenceNumber=" + sequenceNumber + "]";
  }
}
