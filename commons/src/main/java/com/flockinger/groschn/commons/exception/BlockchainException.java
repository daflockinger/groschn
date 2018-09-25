package com.flockinger.groschn.commons.exception;

/**
 * Base class for all exceptions within the Groschn blockchain module.
 *
 */
public class BlockchainException extends RuntimeException {
  /**
   * 
   */
  private static final long serialVersionUID = -147162206867349800L;

  public BlockchainException(String message, Throwable cause) {
    super(message, cause);
  }

  public BlockchainException(String message) {
    super(message);
  }

  public BlockchainException(Throwable cause) {
    super(cause);
  }
}
