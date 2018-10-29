package com.flockinger.groschn.blockchain.exception;

import com.flockinger.groschn.commons.exception.BlockchainException;

public class TransactionNotFoundException extends BlockchainException {
  /**
   * 
   */
  private static final long serialVersionUID = 8032235286765525432L;

  public TransactionNotFoundException(String message) {
    super(message);
  }
}
