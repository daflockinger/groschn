package com.flockinger.groschn.messaging.inbound;

import java.io.Serializable;

public interface SubscriptionService<T extends Serializable> {

  void subscribe(MessageListener<T> listener);
  
  void subscribe(MessageResponder<T> responder);
}
