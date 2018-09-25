package com.flockinger.groschn.commons.model;

import java.util.ArrayList;
import java.util.List;

public class BlockInfoResult {
  
  private long startPosition = 0l;
  private List<BlockInfo> correctInfos = new ArrayList<>();

  public long getStartPosition() {
    return startPosition;
  }

  public void setStartPosition(long startPosition) {
    this.startPosition = startPosition;
  }

  public List<BlockInfo> getCorrectInfos() {
    return correctInfos;
  }
}
