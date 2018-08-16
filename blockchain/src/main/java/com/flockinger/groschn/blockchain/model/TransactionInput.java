package com.flockinger.groschn.blockchain.model;

public class TransactionInput extends TransactionOutput {
  /**
  * 
  */
  private static final long serialVersionUID = 2651960220715786130L;

  private String signature;

  private TransactionPointCut previousOutputTransaction;

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  public TransactionPointCut getPreviousOutputTransaction() {
    return previousOutputTransaction;
  }

  public void setPreviousOutputTransaction(TransactionPointCut previousOutputTransaction) {
    this.previousOutputTransaction = previousOutputTransaction;
  }
}
