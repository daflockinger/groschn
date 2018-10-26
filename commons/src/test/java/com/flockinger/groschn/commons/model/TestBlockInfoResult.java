package com.flockinger.groschn.commons.model;

import java.util.ArrayList;
import java.util.List;

public class TestBlockInfoResult {
  
  private long startPosition = 0l;
  private List<TestBlockInfo> correctInfos = new ArrayList<>();

  public long getStartPosition() {
    return startPosition;
  }

  public void setStartPosition(long startPosition) {
    this.startPosition = startPosition;
  }

  public List<TestBlockInfo> getCorrectInfos() {
    return correctInfos;
  }
}
