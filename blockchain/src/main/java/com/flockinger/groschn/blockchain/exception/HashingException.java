package com.flockinger.groschn.blockchain.exception;

import com.flockinger.groschn.commons.exception.BlockchainException;

public class HashingException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = -1024459701177724181L;

  public HashingException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public HashingException(String message) {
    super(message);
  }
}
