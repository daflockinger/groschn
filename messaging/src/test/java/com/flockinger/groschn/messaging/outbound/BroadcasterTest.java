package com.flockinger.groschn.messaging.outbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import com.flockinger.groschn.commons.compress.CompressedEntity;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.ExecutorConfig;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.impl.BroadcasterImpl;
import io.atomix.cluster.ClusterMembershipService;
import io.atomix.cluster.Member;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import io.atomix.cluster.messaging.MessagingService;
import io.atomix.core.Atomix;
import io.atomix.utils.net.Address;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BroadcasterImpl.class})
@Import(ExecutorConfig.class)
@TestPropertySource(properties="blockchain.messaging.response-timeout=10")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class})
public class BroadcasterTest {

  @Autowired
  private Broadcaster<MessagePayload> broadcaster;
  
  @MockBean
  private Atomix mockTomix;
  @MockBean
  private BlockSerializer serializer;
  
  private final ClusterCommunicationService comMock = mock(ClusterCommunicationService.class);
  private final MessagingService msgMock = mock(MessagingService.class);
  private final ClusterMembershipService memberMock = mock(ClusterMembershipService.class);
  
  @Before
  public void setup() {
    when(mockTomix.getCommunicationService()).thenReturn(comMock);
    when(mockTomix.getMessagingService()).thenReturn(msgMock);
    when(mockTomix.getMembershipService()).thenReturn(memberMock);
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testBroadcast_withNormalEntity_shouldCallSend() {
    var message = new Message<MessagePayload>();
    message.setId(UUID.randomUUID().toString());
    var somePayLoad = new MessagePayload();
    somePayLoad.setSenderId("groschn1");
    var compressedEntity = new CompressedEntity();
    somePayLoad.setEntity(compressedEntity.originalSize(1).entity(new byte[1]));
    message.setPayload(somePayLoad);
    message.setTimestamp(123l);
    
    broadcaster.broadcast(message, MainTopics.BLOCK_INFO);
    
    var subjectCaptor = ArgumentCaptor.forClass(String.class);
    var messageCaptor = ArgumentCaptor.forClass(Message.class);
    var encoderCaptor = ArgumentCaptor.forClass(Function.class);
    verify(comMock).broadcast(subjectCaptor.capture(), messageCaptor.capture(), encoderCaptor.capture());
    assertNotNull("verify that encoder is not null", encoderCaptor.getValue());
    assertEquals("verify that the topic is correct", MainTopics.BLOCK_INFO.name(), subjectCaptor.getValue());
    var sentMessage = (Message<MessagePayload>)messageCaptor.getValue();
    assertEquals("verify that the message has correct ID", message.getId(), sentMessage.getId());
    assertNotNull("verify that the message has a timestamp", sentMessage.getTimestamp());
    var payload = sentMessage.getPayload();
    assertNotNull("verify that the payload is not null", payload);
    assertEquals("verify that the message has", somePayLoad.getSenderId(), payload.getSenderId());
    assertNotNull("verify that the compressed entity is not null", payload.getEntity());
    assertTrue("verify that compressent entity has some size", payload.getEntity().getOriginalSize() > 0);
    assertNotNull("verify that compressent entity is not empty", payload.getEntity().getEntity());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSendRequest_withNormalEntity_shouldCallSend() {
    var message = new Message<MessagePayload>();
    message.setId(UUID.randomUUID().toString());
    var somePayLoad = new MessagePayload();
    somePayLoad.setSenderId("groschn1");
    var compressedEntity = new CompressedEntity();
    somePayLoad.setEntity(compressedEntity.originalSize(1).entity(new byte[1]));
    message.setPayload(somePayLoad);
    message.setTimestamp(123l);
    when(memberMock.getMember(anyString())).thenReturn(Member.member("anotherGroschn", "1.2.3.4:8080"));
    when(msgMock.sendAndReceive(any(), any(), any(), any(), any())).thenReturn(CompletableFuture.supplyAsync(() -> new byte[0]));
    when(serializer.deserialize(any(), any(Class.class))).thenReturn(new Message<MessagePayload>());
    
    
    var response = broadcaster.sendRequest(message, "anotherGroschn", MainTopics.BLOCK_INFO);
    
    assertNotNull("verify response is not null", response);
    assertNotNull("verify that result is not null", response.join());
    
    var subjectCaptor = ArgumentCaptor.forClass(String.class);
    var addressCaptor = ArgumentCaptor.forClass(Address.class);
    var timeoutCaptor = ArgumentCaptor.forClass(Duration.class);
    verify(msgMock).sendAndReceive(addressCaptor.capture(), subjectCaptor.capture(), any(), timeoutCaptor.capture(), any());
    assertNotNull("verify that receiver address is not null", addressCaptor.getValue());
    assertEquals("verify that the topic is correct", MainTopics.BLOCK_INFO.name(), subjectCaptor.getValue());
    assertEquals("verify receiver host is correct", "1.2.3.4", addressCaptor.getValue().host());
    assertEquals("verify receiver port is correct", 8080, addressCaptor.getValue().port());
    assertTrue("verify timeout duration is correct", timeoutCaptor.getValue().compareTo(Duration.ofSeconds(10)) == 0);
    
    var messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(serializer).serialize(messageCaptor.capture());
    var sentMessage = (Message<MessagePayload>)messageCaptor.getValue();
    assertEquals("verify that the message has correct ID", message.getId(), sentMessage.getId());
    assertNotNull("verify that the message has a timestamp", sentMessage.getTimestamp());
    var payload = sentMessage.getPayload();
    assertNotNull("verify that the payload is not null", payload);
    assertEquals("verify that the message has", somePayLoad.getSenderId(), payload.getSenderId());
    assertNotNull("verify that the compressed entity is not null", payload.getEntity());
    assertTrue("verify that compressent entity has some size", payload.getEntity().getOriginalSize() > 0);
    assertNotNull("verify that compressent entity is not empty", payload.getEntity().getEntity());
  }
}
