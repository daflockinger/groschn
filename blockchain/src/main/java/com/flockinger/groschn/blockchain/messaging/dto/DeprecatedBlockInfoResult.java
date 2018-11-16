package com.flockinger.groschn.blockchain.messaging.dto;

import java.util.ArrayList;
import java.util.List;

public class DeprecatedBlockInfoResult {
  
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
