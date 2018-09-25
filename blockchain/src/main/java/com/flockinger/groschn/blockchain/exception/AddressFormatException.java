package com.flockinger.groschn.blockchain.exception;

import com.flockinger.groschn.commons.exception.BlockchainException;

public class AddressFormatException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 169793225847553307L;

  public AddressFormatException(String message) {
    super(message);
  }
}
