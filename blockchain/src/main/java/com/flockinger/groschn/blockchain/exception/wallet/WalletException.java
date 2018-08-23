package com.flockinger.groschn.blockchain.exception.wallet;

import com.flockinger.groschn.blockchain.exception.BlockchainException;

public abstract class WalletException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 6636007665257524941L;

  public WalletException(String message, Throwable cause) {
    super(message, cause);
  }

  public WalletException(String message) {
    super(message);
  }

  public WalletException(Throwable cause) {
    super(cause);
  }
}
