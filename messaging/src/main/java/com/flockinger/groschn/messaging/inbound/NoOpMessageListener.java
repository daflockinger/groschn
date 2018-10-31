package com.flockinger.groschn.messaging.inbound;

import java.util.Optional;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;

public class NoOpMessageListener implements MessageListener<MessagePayload>, MessageResponder<MessagePayload> {

  @Override
  public Optional<Message<MessagePayload>> respond(Message<MessagePayload> request) {
    return Optional.empty();
  }

  @Override
  public void receiveMessage(Message<MessagePayload> message) {
  }

  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.NONE;
  }

}
