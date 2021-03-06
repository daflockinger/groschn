package com.flockinger.groschn.messaging.model;

import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.flockinger.groschn.blockchain.model.Hashable;

public class SyncRequest implements Hashable<SyncRequest> {
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
  
  private List<RequestHeader> wantedHeaders;
  
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
  
  public List<RequestHeader> getWantedHeaders() {
    return wantedHeaders;
  }

  public void setWantedHeaders(List<RequestHeader> wantedHeaders) {
    this.wantedHeaders = wantedHeaders;
  }

  @Override
  public int compareTo(SyncRequest o) {
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
