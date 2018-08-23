package com.flockinger.groschn.blockchain.dto;

public class WalletSecretDto {
  private String secretKey;
  private String salt;
  
  public String getSecretKey() {
    return secretKey;
  }
  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }
  public String getSalt() {
    return salt;
  }
  public void setSalt(String salt) {
    this.salt = salt;
  }
}
