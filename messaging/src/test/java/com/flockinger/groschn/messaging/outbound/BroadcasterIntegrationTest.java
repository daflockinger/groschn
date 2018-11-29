package com.flockinger.groschn.messaging.outbound;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.impl.BroadcasterImpl;
import com.flockinger.groschn.messaging.util.MessagingContext;
import com.google.common.collect.ImmutableList;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.util.concurrent.Executor;
import org.junit.Test;
import org.springframework.stereotype.Component;

public class BroadcasterIntegrationTest {

  private MessagingContext utilsMock = mock(MessagingContext.class);
  private ClusterCommunicationService clusterCommunicationService = mock(ClusterCommunicationService.class);
  
  @Test
  public void testInit_shouldHaveCalledCorrectly() {
    var serializerMock = mock(BlockSerializer.class);
    when(utilsMock.serializer()).thenReturn(serializerMock);
    when(utilsMock.executor()).thenReturn(mock(Executor.class));
   when(utilsMock.clusterCommunicationService()).thenReturn(clusterCommunicationService);

    new BroadcasterImpl(ImmutableList.of(new MickeyMouseResponder(), new GoofyResponder()), utilsMock);

    verify(clusterCommunicationService).subscribe(eq(MainTopics.SYNC_BLOCKCHAIN.name()), any(), notNull(), any(), any(Executor.class));
    verify(clusterCommunicationService).subscribe(eq(MainTopics.SYNC_TRANSACTIONS.name()), any(), notNull(), any(), any(Executor.class));
  }

  @Component
  public final static class MickeyMouseResponder implements MessageResponder<MessagePayload> {
    @Override
    public Message<MessagePayload> respond(Message<MessagePayload> request) {
      return new Message<>();
    }
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.SYNC_BLOCKCHAIN;
    }
  }
  @Component
  public final static class GoofyResponder implements MessageResponder<MessagePayload> {

    @Override
    public Message<MessagePayload> respond(Message<MessagePayload> request) {
      return new Message<>();
    }
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.SYNC_TRANSACTIONS;
    }
  }
}
