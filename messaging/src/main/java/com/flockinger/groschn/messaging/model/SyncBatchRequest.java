package com.flockinger.groschn.messaging.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.flockinger.groschn.messaging.config.MainTopics;

public class SyncBatchRequest {
  
  @Min(1)
  private long fromPosition;
  @NotNull
  private MainTopics topic;
  @Min(1)
  private int idealReceiveNodeCount;
  @Min(1)
  private int maxFetchRetries;
  @Min(5)
  private int batchSize;
  
  public static SyncBatchRequest build() {
    return new SyncBatchRequest();
  }
  
  public static SyncBatchRequest build(SyncBatchRequest oldRequest) {
    var request = new SyncBatchRequest();  
    return request.batchSize(oldRequest.getBatchSize())
        .idealReceiveNodeCount(oldRequest.getIdealReceiveNodeCount())
        .maxFetchRetries(oldRequest.getMaxFetchRetries())
        .topic(oldRequest.getTopic());
  }
  
  public long getFromPosition() {
    return fromPosition;
  }
  public void setFromPosition(long fromPosition) {
    this.fromPosition = fromPosition;
  }
  public SyncBatchRequest fromPosition(long fromPosition) {
    this.fromPosition = fromPosition;
    return this;
  }
  public MainTopics getTopic() {
    return topic;
  }
  public SyncBatchRequest topic(MainTopics topic) {
    this.topic = topic;
    return this;
  }
  public int getIdealReceiveNodeCount() {
    return idealReceiveNodeCount;
  }
  public SyncBatchRequest idealReceiveNodeCount(int idealReceiveNodeCount) {
    this.idealReceiveNodeCount = idealReceiveNodeCount;
    return this;
  }
  public int getMaxFetchRetries() {
    return maxFetchRetries;
  }
  public SyncBatchRequest maxFetchRetries(int maxFetchRetries) {
    this.maxFetchRetries = maxFetchRetries;
    return this;
  }
  public int getBatchSize() {
    return batchSize;
  }
  public SyncBatchRequest batchSize(int batchSize) {
    this.batchSize = batchSize;
    return this;
  }
}
