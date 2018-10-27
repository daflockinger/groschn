package com.flockinger.groschn.messaging.inbound;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.exception.BlockchainException;
import com.flockinger.groschn.messaging.exception.ReceivedMessageInvalidException;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import com.github.benmanes.caffeine.cache.Cache;

public abstract class AbstractMessageListener<T extends Hashable<T>> 
                          implements MessageListener<MessagePayload> {
  @Autowired
  private MessagingUtils messageUtils;
  
  private final static Logger LOG = LoggerFactory.getLogger(AbstractMessageListener.class);
  
  protected abstract void handleMessage(T message);
  
  protected abstract Cache<String, String> getCache();
  protected abstract Class<T> messageBodyType();
  
  
  public void receiveMessage(Message<MessagePayload> message) {
    try {
      messageUtils.assertEntity(message);
      assertMessageIsNew(message.getId());
      Optional<T> extractedMessage = messageUtils.extractPayload(message, messageBodyType());
      if (extractedMessage.isPresent()) {
        LOG.info("{} message received.", messageBodyType().getSimpleName());
        handleMessage(extractedMessage.get());
      }
    } catch (BlockchainException e) {
      LOG.error("Invalid {}-Message received: " + e.getMessage(), e, messageBodyType().getSimpleName());
    }
  }
  
  private void assertMessageIsNew(String messageId) {
    Optional<String> existingKey = Optional.ofNullable(getCache().getIfPresent(messageId));
    if(!existingKey.isPresent()) {
      getCache().put(messageId, messageId);
    } else {
      throw new ReceivedMessageInvalidException("Message was already received with ID: " + messageId);
    }
  }
}
