package com.flockinger.groschn.messaging.sync;

import com.flockinger.groschn.messaging.model.RequestParams;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class SyncRequester {

  @Autowired
  private Broadcaster broadcaster;

  @Value("${atomix.node-id}")
  private String nodeId;

  @SuppressWarnings("unchecked")
  public CompletableFuture<Optional<SyncResponse>> doRequest(Entry<String, SyncBatchRequest> requestEntity) {
    var request = requestEntity.getValue();
    var requestParams = RequestParams.build(createPayload(request)).senderId(nodeId).receiverNodeId(requestEntity.getKey()).topic(request.getTopic());

    return broadcaster.sendRequest(requestParams, SyncResponse.class);
  }

  private SyncRequest createPayload(SyncBatchRequest batchRequest) {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(batchRequest.getFromPosition());
    request.setRequestPackageSize(Integer.toUnsignedLong(batchRequest.getBatchSize()));
    request.setWantedHeaders(batchRequest.getWantedHeaders());
    return request;
  }
}
