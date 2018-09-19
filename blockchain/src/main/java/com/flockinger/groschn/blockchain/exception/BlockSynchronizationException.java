package com.flockinger.groschn.blockchain.exception;

public class BlockSynchronizationException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 6896777381997435801L;

  public BlockSynchronizationException(String message, Throwable cause) {
    super(message, cause);
  }

  public BlockSynchronizationException(String message) {
    super(message);
  }

  public BlockSynchronizationException(Throwable cause) {
    super(cause);
  }
}
