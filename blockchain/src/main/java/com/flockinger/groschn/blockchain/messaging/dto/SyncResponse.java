package com.flockinger.groschn.blockchain.messaging.dto;

import java.io.Serializable;
import java.util.List;
import com.flockinger.groschn.blockchain.model.Hashable;

public class SyncResponse<T extends Serializable> implements Hashable{
  /**
   * 
   */
  private static final long serialVersionUID = -4858493979309156663L;

  private Long startingPosition;
  
  private List<T> entities;
    
  private boolean lastPositionReached = false;

  public Long getStartingPosition() {
    return startingPosition;
  }

  public void setStartingPosition(Long startingPosition) {
    this.startingPosition = startingPosition;
  }

  public List<T> getEntities() {
    return entities;
  }

  public void setEntities(List<T> entities) {
    this.entities = entities;
  }

  public boolean isLastPositionReached() {
    return lastPositionReached;
  }

  public void setLastPositionReached(boolean lastPositionReached) {
    this.lastPositionReached = lastPositionReached;
  }
}