package com.flockinger.groschn.messaging.exception;

import com.flockinger.groschn.commons.exception.BlockchainException;

public class ReceivedMessageInvalidException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = -6922458088849578264L;

  public ReceivedMessageInvalidException(String message) {
    super(message);
  }
}
