package com.flockinger.groschn.blockchain.dto;

public class WalletDto {
  
  private String publicKey; 
  private String walletEncryptionKey;
  
  public String getPublicKey() {
    return publicKey;
  }
  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }
  public String getWalletEncryptionKey() {
    return walletEncryptionKey;
  }
  public void setWalletEncryptionKey(String walletEncryptionKey) {
    this.walletEncryptionKey = walletEncryptionKey;
  }
  
  public static WalletDto build() {
    return new WalletDto();
  }
  
  public WalletDto publicKey(String publicKey) {
    this.publicKey = publicKey;
    return this;
  }
  
  public WalletDto walletEncryptionKey(String walletEncryptionKey) {
    this.walletEncryptionKey = walletEncryptionKey;
    return this;
  }
}
