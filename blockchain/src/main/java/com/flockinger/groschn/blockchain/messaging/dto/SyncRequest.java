package com.flockinger.groschn.blockchain.messaging.dto;

import javax.validation.constraints.Min;
import com.esotericsoftware.kryo.NotNull;
import com.flockinger.groschn.blockchain.model.Hashable;

public class SyncRequest implements Hashable {
  /**
   * 
   */
  private static final long serialVersionUID = 7299924860650197421L;
  
  @NotNull
  @Min(1)
  private Long startingPosition;
  
  public Long getStartingPosition() {
    return startingPosition;
  }

  public void setStartingPosition(Long startingPosition) {
    this.startingPosition = startingPosition;
  }
}
