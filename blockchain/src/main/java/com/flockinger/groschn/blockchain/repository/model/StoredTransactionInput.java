package com.flockinger.groschn.blockchain.repository.model;

import java.math.BigDecimal;

public class StoredTransactionInput {
  
  private BigDecimal amount;
  
  private String publicKey;
  
  private Long timestamp;
  
  private Long sequenceNumber;
  
  private String signature;
  
  public String getSignature() {
      return signature;
  }

  public void setSignature(String signature) {
      this.signature = signature;
  }

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
}
