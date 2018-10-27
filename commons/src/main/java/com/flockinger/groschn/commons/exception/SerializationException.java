package com.flockinger.groschn.commons.exception;


public class SerializationException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = -679942310894819231L;

  public SerializationException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public SerializationException(String message) {
    super(message);
  }
}
