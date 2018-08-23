package com.flockinger.groschn.blockchain.exception.crypto;

public class CantConfigureSigningAlgorithmException extends CryptoException {
  /**
   * 
   */
  private static final long serialVersionUID = -2612329916489346150L;

  public CantConfigureSigningAlgorithmException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public CantConfigureSigningAlgorithmException(String message) {
    super(message);
  }
}
