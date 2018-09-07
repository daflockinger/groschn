package com.flockinger.groschn.blockchain.exception.validation.transaction;

public class TransactionInputMissingOutputBalanceException extends TransactionException {
  /**
   * 
   */
  private static final long serialVersionUID = 4866454428596384721L;

  public TransactionInputMissingOutputBalanceException(String message) {
    super(message);
  }
}
