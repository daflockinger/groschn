package com.flockinger.groschn.blockchain.repository.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "wallet")
public class StoredWallet {

  @Id
  private String id;
  
  @Indexed(unique=true)
  private String publicKey;
  
  private byte[] initVector;
  
  private byte[] encryptedPrivateKey;

  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public byte[] getInitVector() {
    return initVector;
  }

  public void setInitVector(byte[] initVector) {
    this.initVector = initVector;
  }

  public byte[] getEncryptedPrivateKey() {
    return encryptedPrivateKey;
  }

  public void setEncryptedPrivateKey(byte[] encryptedPrivateKey) {
    this.encryptedPrivateKey = encryptedPrivateKey;
  }
}
