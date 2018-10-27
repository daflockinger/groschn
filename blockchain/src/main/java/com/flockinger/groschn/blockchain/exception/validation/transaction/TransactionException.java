package com.flockinger.groschn.blockchain.exception.validation.transaction;

import com.flockinger.groschn.commons.exception.BlockchainException;

public abstract class TransactionException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 1435536782515165207L;

  public TransactionException(String message, Throwable cause) {
    super(message, cause);
  }

  public TransactionException(String message) {
    super(message);
  }

  public TransactionException(Throwable cause) {
    super(cause);
  }
}
