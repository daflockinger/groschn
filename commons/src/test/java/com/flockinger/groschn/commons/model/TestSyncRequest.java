package com.flockinger.groschn.commons.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.flockinger.groschn.blockchain.model.Hashable;

public class TestSyncRequest implements Hashable<TestSyncRequest> {
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

  @Override
  public int compareTo(TestSyncRequest o) {
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
