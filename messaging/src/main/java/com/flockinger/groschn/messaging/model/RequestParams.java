package com.flockinger.groschn.messaging.model;

import com.flockinger.groschn.messaging.config.MainTopics;

public class RequestParams {
  private SyncRequest syncRequest;
  private String senderId;
  private String receiverNodeId;
  private  MainTopics topic;

  private RequestParams () {}

  public static  RequestParams build(SyncRequest syncRequest) {
    var params = new RequestParams();
    params.syncRequest = syncRequest;
    return params;
  }

  public SyncRequest getSyncRequest() {
    return syncRequest;
  }

  public String getSenderId() {
    return senderId;
  }

  public RequestParams senderId(String senderId) {
    this.senderId = senderId;
    return this;
  }

  public String getReceiverNodeId() {
    return receiverNodeId;
  }

  public RequestParams receiverNodeId(String receiverNodeId) {
    this.receiverNodeId = receiverNodeId;
    return this;
  }

  public String getTopic() {
    return topic.name();
  }

  public RequestParams topic(MainTopics topic) {
    this.topic = topic;
    return this;
  }
}
