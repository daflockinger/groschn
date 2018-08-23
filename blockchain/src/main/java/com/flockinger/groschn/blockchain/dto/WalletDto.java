package com.flockinger.groschn.blockchain.dto;

public class WalletDto {
  
  private String publicKey;
  private String encryptedPrivateKey;
  
  public String getPublicKey() {
    return publicKey;
  }
  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }
  public String getEncryptedPrivateKey() {
    return encryptedPrivateKey;
  }
  public void setEncryptedPrivateKey(String encryptedPrivateKey) {
    this.encryptedPrivateKey = encryptedPrivateKey;
  }
}
