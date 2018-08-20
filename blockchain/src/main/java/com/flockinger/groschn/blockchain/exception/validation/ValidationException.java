package com.flockinger.groschn.blockchain.exception.validation;

import com.flockinger.groschn.blockchain.exception.BlockchainException;

public abstract class ValidationException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 9132574673998363878L;

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
