package com.flockinger.groschn.blockchain.model;

import java.io.Serializable;
import java.util.List;
import com.flockinger.groschn.blockchain.model.Hashable;

public class SyncResponse<T extends Serializable> implements Hashable<SyncResponse<T>>{
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

  @Override
  public int compareTo(SyncResponse<T> o) {
    if (this.getStartingPosition() == null && o.getStartingPosition() == null) {
      return 0;
    } else if (this.getStartingPosition() == null) {
      return -1;
    } else if (o.getStartingPosition() == null) {
      return 1;
    }
    return this.getStartingPosition().compareTo(o.getStartingPosition());
  }
}
