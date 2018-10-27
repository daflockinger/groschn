package com.flockinger.groschn.commons.model;

import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class TestMessage<T extends Serializable> {
 
  @NotNull
  private String id;
  @Min(1)
  @NotNull
  private Long timestamp;
  @NotNull
  @Valid
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
