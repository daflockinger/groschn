package com.flockinger.groschn.messaging.model;

import java.io.Serializable;

public class Message<T extends Serializable> {
 
  private String id;
  private Long timestamp;
  private T payload;
  
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
  public T getPayload() {
    return payload;
  }
  public void setPayload(T payload) {
    this.payload = payload;
  }
  
}
