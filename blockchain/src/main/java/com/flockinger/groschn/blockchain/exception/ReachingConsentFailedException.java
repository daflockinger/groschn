package com.flockinger.groschn.blockchain.exception;

import com.flockinger.groschn.commons.exception.BlockchainException;

public class ReachingConsentFailedException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 7008259826135115645L;

  public ReachingConsentFailedException(String message) {
    super(message);
  }
}
