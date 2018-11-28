package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ScanningContext {
  private List<BlockInfoResult> finalResults;
  private Long fromPosition;
  private Optional<BlockInfoResult> currentResult;
  private Integer batchSize;
  
  private ScanningContext(List<BlockInfoResult> finalResults) {
    this.finalResults = finalResults;
  }
  
  public static ScanningContext build(List<BlockInfoResult> finalResults) {
    var context = new ScanningContext(finalResults);
    return context;
  }

  public void addFinalResult(Optional<BlockInfoResult> result) {
    finalResults.addAll(result.stream().collect(Collectors.toList()));
  }

  public Long getFromPosition() {
    return fromPosition;
  }

  public ScanningContext fromPosition(Long fromPosition) {
    this.fromPosition = fromPosition;
    return this;
  }

  public Optional<BlockInfoResult> current() {
    return currentResult;
  }

  public ScanningContext currentResult(Optional<BlockInfoResult> currentResult) {
    this.currentResult = currentResult;
    return this;
  }

  public Integer getBatchSize() {
    return batchSize;
  }

  public ScanningContext batchSize(Integer batchSize) {
    this.batchSize = batchSize;
    return this;
  }
}
