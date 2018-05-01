package com.flockinger.groschn.messaging.inbound;

import java.io.Serializable;
import com.flockinger.groschn.messaging.model.Message;

public interface MessageListener<T extends Serializable> {

  void receiveMessage(Message<T> message);
  
  String getSubscribedTopic();
}
