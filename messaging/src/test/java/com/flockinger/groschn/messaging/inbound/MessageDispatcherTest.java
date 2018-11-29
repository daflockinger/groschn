package com.flockinger.groschn.messaging.inbound;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.util.MessagingContext;
import com.google.common.collect.ImmutableList;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.junit.Test;
import org.springframework.stereotype.Component;

public class MessageDispatcherTest  {

  private MessagingContext utilsMock = mock(MessagingContext.class);
  
  @Test
  public void testInit_shouldHaveCalledCorrectly() {
    var clusterCommunicationService = mock(ClusterCommunicationService.class);
    var serializerMock = mock(BlockSerializer.class);
    when(utilsMock.serializer()).thenReturn(serializerMock);
    when(utilsMock.executor()).thenReturn(mock(Executor.class));
    when(utilsMock.clusterCommunicationService()).thenReturn(clusterCommunicationService);

    new MessageDispatcher(ImmutableList.of(new PlutoListener(), new DonaldDuckListener()),  utilsMock);

    verify(clusterCommunicationService).subscribe(eq(MainTopics.FRESH_TRANSACTION.name()), notNull(), notNull(Consumer.class), any(Executor.class));
    verify(clusterCommunicationService).subscribe(eq(MainTopics.FRESH_BLOCK.name()),notNull(), notNull(Consumer.class), any(Executor.class));
  }

  @Component
  public final static class PlutoListener implements MessageListener<MessagePayload> {
    @Override
    public void receiveMessage(Message<MessagePayload> message) {      
    }
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.FRESH_TRANSACTION;
    }
  }
  @Component
  public final static class DonaldDuckListener implements MessageListener<MessagePayload> {
    @Override
    public void receiveMessage(Message<MessagePayload> message) {      
    }
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.FRESH_BLOCK;
    }
  }
}
