package com.flockinger.groschn.messaging.inbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import org.junit.Test;

public class NoOpMessageListenerTest {

  private NoOpMessageListener listener = new NoOpMessageListener();

  @Test
  public void testRespond_shouldReturnEmptyMessage() {
    assertNotNull("verify that it doesn't respond null", listener.respond(new Message<>()));
  }

  @Test
  public void testReceiveMessage_shouldDoNothing() {
    listener.receiveMessage(new Message<>());
  }

  @Test
  public void testGetSubscribedTopic() {
    assertEquals("verify that it's subscribed to NONE", MainTopics.NONE, listener.getSubscribedTopic());
  }
}