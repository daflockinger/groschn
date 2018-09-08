package com.flockinger.groschn.blockchain.dto;

import java.io.Serializable;
import com.flockinger.groschn.blockchain.util.CompressedEntity;

public class MessagePayload implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1278143685516903764L;
  
  private String senderId;
  private CompressedEntity entity;
  
  public String getSenderId() {
    return senderId;
  }
  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }
  public CompressedEntity getEntity() {
    return entity;
  }
  public void setEntity(CompressedEntity entity) {
    this.entity = entity;
  }
}
