package com.flockinger.groschn.commons.exception.crypto;

import com.flockinger.groschn.commons.exception.BlockchainException;

public abstract class CryptoException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = -7410715786130870613L;

  public CryptoException(String message) {
    super(message);
  }
  
  public CryptoException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public CryptoException(Throwable cause) {
    super(cause);
  }
}
