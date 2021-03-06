package com.flockinger.groschn.messaging.inbound;

import java.io.Serializable;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;

public interface MessageResponder <T extends Serializable> {

  Message<T> respond(Message<T> request);
  
  MainTopics getSubscribedTopic();
}
