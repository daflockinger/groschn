package com.flockinger.groschn.blockchain.transaction.impl;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.messaging.MessageReceiverUtils;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageListener;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;

@Component
public class TransactionPoolListener implements MessageListener<MessagePayload> {

  @Autowired
  private MessageReceiverUtils messageUtils;
  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  @Qualifier("TransactionId_Cache")
  private Cache<String, String> transactionIdCache;
  
  private final static Logger LOG = LoggerFactory.getLogger(TransactionPoolListener.class);
  
  @Override
  public void receiveMessage(Message<MessagePayload> message) {
    try {
      messageUtils.assertMessage(message);
      assertMessageIsNew(message.getId());
      Optional<Transaction> transaction = messageUtils.extractPayload(message, Transaction.class);
      if (transaction.isPresent()) {
        LOG.info("Fresh Transaction received by sender: " + message.getPayload().getSenderId());
        transactionManager.storeTransaction(transaction.get());
      }
    } catch (BlockchainException e) {
      LOG.error("Invalid Transaction-Message received: " + e.getMessage(), e);
    }
  }
  
  private void assertMessageIsNew(String messageId) {
    Optional<String> existingKey = Optional.ofNullable(transactionIdCache.getIfPresent(messageId));
    if(!existingKey.isPresent()) {
      transactionIdCache.put(messageId, messageId);
    } else {
      throw new ReceivedMessageInvalidException("Transaction Message was already received with ID: " + messageId);
    }
  }
  
  @Override
  public String getSubscribedTopic() {
    return MainTopics.FRESH_TRANSACTION.name();
  }
}
