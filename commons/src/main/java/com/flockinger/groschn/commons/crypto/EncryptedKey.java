package com.flockinger.groschn.commons.crypto;

public class EncryptedKey {
  
  private byte[] key;
  private byte[] initVector;
  
  private EncryptedKey() {}
  
  public static EncryptedKey build() {
    return new EncryptedKey();
  }
  
  public byte[] getKey() {
    return key;
  }
  public EncryptedKey key(byte[] key) {
    this.key = key;
    return this;
  }
  public byte[] getInitVector() {
    return initVector;
  }
  public EncryptedKey initVector(byte[] initVector) {
    this.initVector = initVector;
    return this;
  }
}
