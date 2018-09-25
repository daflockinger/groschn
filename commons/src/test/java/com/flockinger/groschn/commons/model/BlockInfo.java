package com.flockinger.groschn.commons.model;

import java.util.Objects;
import com.flockinger.groschn.blockchain.model.Hashable;

public class BlockInfo implements Hashable<BlockInfo> {
  /**
   * 
   */
  private static final long serialVersionUID = -1091523569509085021L;
  
  private String blockHash;
  private Long position;
  
  public String getBlockHash() {
    return blockHash;
  }
  public void setBlockHash(String blockHash) {
    this.blockHash = blockHash;
  }
  public Long getPosition() {
    return position;
  }
  public void setPosition(Long position) {
    this.position = position;
  }
  
  @Override
  public int compareTo(BlockInfo o) {
    if(this.getPosition() == null && o.getPosition() == null) {
      return 0;
    } else if (this.getPosition() == null) {
      return -1;
    } else if (o.getPosition() == null) {
      return 1;
    }
    return this.getPosition().compareTo(o.getPosition());
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj == null) {
      return false;
    }
    if(!(obj instanceof BlockInfo)) {
      return false;
    }
    return Objects.equals(this.getBlockHash(),((BlockInfo)obj).getBlockHash())
    && Objects.equals(this.getPosition(),((BlockInfo)obj).getPosition());
  }
}
