package com.flockinger.groschn.messaging.outbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.commons.compress.CompressionUtils;
import com.flockinger.groschn.messaging.ExecutorConfig;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageDispatcher;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.RequestParams;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.outbound.impl.BroadcasterImpl;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import com.flockinger.groschn.messaging.util.TestBlock;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BroadcasterImpl.class, BroadcasterTest.GarfieldResponder.class, MessagingUtils.class, CompressionUtils.class})
@Import(ExecutorConfig.class)
@TestPropertySource(properties="blockchain.messaging.response-timeout=10")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class})
public class BroadcasterTest {

  @Autowired
  private Broadcaster broadcaster;
  
  @MockBean
  private ClusterCommunicationService clusterCommunicationService;
  @MockBean
  private MessageDispatcher mockDispatcher;
  @Autowired
  private MessagingUtils utils;
  
  @Test
  @SuppressWarnings("unchecked")
  public void testBroadcast_withNormalEntity_shouldCallSend() {
    var testBlock = new TestBlock();
    testBlock.setHash("1234");
    
    broadcaster.broadcast(testBlock, "GarfieldRespondergroschn1", MainTopics.BLOCK_INFO);
    
    var subjectCaptor = ArgumentCaptor.forClass(String.class);
    var messageCaptor = ArgumentCaptor.forClass(Message.class);
    var encoderCaptor = ArgumentCaptor.forClass(Function.class);
    verify(clusterCommunicationService).broadcast(subjectCaptor.capture(), messageCaptor.capture(), encoderCaptor.capture());
    assertNotNull("verify that encoder is not null", encoderCaptor.getValue());
    assertEquals("verify that the topic is correct", MainTopics.BLOCK_INFO.name(), subjectCaptor.getValue());
    var sentMessage = (Message<MessagePayload>)messageCaptor.getValue();
    assertNotNull("verify that the message has an ID", sentMessage.getId());
    assertNotNull("verify that the message has a timestamp", sentMessage.getTimestamp());
    var payload = sentMessage.getPayload();
    assertNotNull("verify that the payload is not null", payload);
    assertNotNull("verify that the message has a sender id",payload.getSenderId());
    assertNotNull("verify that the compressed entity is not null", payload.getEntity());
    assertTrue("verify that compressent entity has some size", payload.getEntity().getOriginalSize() > 0);
    assertNotNull("verify that compressent entity is not empty", payload.getEntity().getEntity());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSendRequest_withNormalEntity_shouldCallSend() {
    when(clusterCommunicationService.send(any(), any(), any(),any(),any(), any()))
        .thenReturn(CompletableFuture.supplyAsync(() -> utils.packageMessage(new TestBlock(), "groschn1")));

    var request = new SyncRequest();
    request.setStartingPosition(1L);
    request.setRequestPackageSize(3L);
    var requestParams = RequestParams.build(request).topic(MainTopics.BLOCK_INFO).receiverNodeId("anotherGroschn").senderId("groschn1");

    var response = broadcaster.sendRequest(requestParams, TestBlock.class);

    assertNotNull("verify response is not null", response);
    assertNotNull("verify that result is not null", response.join());
    
    var subjectCaptor = ArgumentCaptor.forClass(String.class);
    var memberCaptor = ArgumentCaptor.forClass(MemberId.class);
    var timeoutCaptor = ArgumentCaptor.forClass(Duration.class);
    verify(clusterCommunicationService).send(subjectCaptor.capture(), any(), any(),any(),memberCaptor.capture(), timeoutCaptor.capture());
    assertEquals("verify that receiver member id is correct", "anotherGroschn", memberCaptor.getValue().id());
    assertEquals("verify that the topic is correct", MainTopics.BLOCK_INFO.name(), subjectCaptor.getValue());
    assertTrue("verify timeout duration is correct", timeoutCaptor.getValue().compareTo(Duration.ofSeconds(10)) == 0);
  }
  
  @Component
  public static class GarfieldResponder implements MessageResponder<MessagePayload> {
    @Override
    public Message<MessagePayload> respond(Message<MessagePayload> request) {
      return new Message<>();
    }
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.NONE;
    }
  }
  
  
}
