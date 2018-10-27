package com.flockinger.groschn.messaging.util;

public class TestTransactionInput extends TestTransactionOutput {
  /**
  * 
  */
  private static final long serialVersionUID = 2651960220715786130L;

  private String signature;


  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  @Override
  public String toString() {
    return "TestTransactionInput [signature=" + signature + ", getAmount()=" + getAmount()
        + ", getPublicKey()=" + getPublicKey() + ", getTimestamp()=" + getTimestamp()
        + ", getSequenceNumber()=" + getSequenceNumber() + "]";
  }
}
