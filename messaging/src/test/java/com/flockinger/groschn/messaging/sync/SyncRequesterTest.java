package com.flockinger.groschn.messaging.sync;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.RequestHeader;
import com.flockinger.groschn.messaging.model.RequestParams;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SyncRequester.class})
@TestPropertySource(properties = "atomix.node-id=groschn1")
public class SyncRequesterTest {

  @MockBean
  private Broadcaster broadcaster;

  @Autowired
  private SyncRequester requester;


  @Test
  public void testDoRequest_withValidRequest_shouldCallBroadcasterCorrectly() {
    var batchRequest = new SyncBatchRequest();
    batchRequest.setFromPosition(10L);
    batchRequest.setWantedHeaders(ImmutableList.of(new RequestHeader()));
    batchRequest.topic(MainTopics.BLOCK_INFO);
    batchRequest.batchSize(12);
    requester.doRequest(ImmutableMap.of("receiverId", batchRequest).entrySet().iterator().next());


    var requestParamCaptor = ArgumentCaptor.forClass(RequestParams.class);
    verify(broadcaster).sendRequest(requestParamCaptor.capture(), eq(SyncResponse.class));

    var params = requestParamCaptor.getValue();
    assertEquals("verify correct sender ID", "groschn1", params.getSenderId());
    assertEquals("verify correct receiver ID", "receiverId", params.getReceiverNodeId());
    assertEquals("verify correct topic", "BLOCK_INFO", params.getTopic());
    var syncRequestEntity = params.getSyncRequest();
    assertEquals("verify sync request starting position", 10L, syncRequestEntity.getStartingPosition().longValue());
    assertEquals("verify sync request batch size" , 12L, syncRequestEntity.getRequestPackageSize().longValue());
    assertEquals("verify sync request wanted headers are present" , 1, syncRequestEntity.getWantedHeaders().size());
  }
}