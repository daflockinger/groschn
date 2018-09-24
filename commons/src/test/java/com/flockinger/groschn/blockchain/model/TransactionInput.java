package com.flockinger.groschn.blockchain.model;

public class TransactionInput extends TransactionOutput {
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
}
