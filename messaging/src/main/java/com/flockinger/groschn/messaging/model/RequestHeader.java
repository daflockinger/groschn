package com.flockinger.groschn.messaging.model;

import java.io.Serializable;

public class RequestHeader implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1771275014607453639L;
  
  private String hash;
  private Long position;
  
  public String getHash() {
    return hash;
  }
  public void setHash(String hash) {
    this.hash = hash;
  }
  public Long getPosition() {
    return position;
  }
  public void setPosition(Long position) {
    this.position = position;
  }
}
