package com.flockinger.groschn.blockchain.model;

public abstract class Transaction implements Hashable {
  
  private String id;
  private Long timestamp;
  
  private String signature;

  
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  public Long getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
  public String getSignature() {
    return signature;
  }
  public void setSignature(String signature) {
    this.signature = signature;
  }
}
