package com.flockinger.groschn.blockchain.exception.crypto;

public class CipherConfigurationException extends CryptoException {
  /**
   * 
   */
  private static final long serialVersionUID = 84720679692753078L;

  public CipherConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public CipherConfigurationException(String message) {
    super(message);
  }
  
  public CipherConfigurationException(Throwable cause) {
    super(cause);
  }
}
