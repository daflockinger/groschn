package com.flockinger.groschn.blockchain.exception;

public class TransactionAlreadyClearedException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 4871500443826612619L;

  public TransactionAlreadyClearedException(String message) {
    super(message);
  }
}
