package com.flockinger.groschn.blockchain.transaction.impl;

import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageListener;
import com.flockinger.groschn.messaging.model.Message;

@Component
public class TransactionPoolListener implements MessageListener<MessagePayload> {

  @Override
  public void receiveMessage(Message<MessagePayload> message) {
    //TODO implement
  }
  
  @Override
  public String getSubscribedTopic() {
    return MainTopics.FRESH_TRANSACTION.name();
  }
}
