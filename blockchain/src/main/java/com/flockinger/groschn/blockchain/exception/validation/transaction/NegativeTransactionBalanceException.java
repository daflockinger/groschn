package com.flockinger.groschn.blockchain.exception.validation.transaction;

public class NegativeTransactionBalanceException extends TransactionException {
  /**
   * 
   */
  private static final long serialVersionUID = 8785891688864545047L;

  public NegativeTransactionBalanceException(String message) {
    super(message);
  }
}
