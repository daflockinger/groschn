package com.flockinger.groschn.blockchain.messaging.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.flockinger.groschn.blockchain.model.Hashable;

public class SyncRequest implements Hashable {
  /**
   * 
   */
  private static final long serialVersionUID = 7299924860650197421L;
  
  @NotNull
  @Min(1)
  private Long startingPosition;
  
  @NotNull
  @Min(1)
  private Long requestPackageSize;
  
  public Long getStartingPosition() {
    return startingPosition;
  }

  public void setStartingPosition(Long startingPosition) {
    this.startingPosition = startingPosition;
  }

  public Long getRequestPackageSize() {
    return requestPackageSize;
  }

  public void setRequestPackageSize(Long requestPackageSize) {
    this.requestPackageSize = requestPackageSize;
  }
}
