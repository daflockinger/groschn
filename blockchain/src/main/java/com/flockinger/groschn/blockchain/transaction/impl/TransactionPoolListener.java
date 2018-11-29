package com.flockinger.groschn.blockchain.transaction.impl;

import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.commons.exception.BlockchainException;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageListener;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TransactionPoolListener implements MessageListener<MessagePayload> {
  
  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  @Qualifier("TransactionId_Cache")
  private Cache<String, String> transactionIdCache;

  @Autowired
  private MessagePackageHelper helper;

  private Logger LOG = LoggerFactory.getLogger(TransactionPoolListener.class);

  @Override
  public void receiveMessage(Message<MessagePayload> message) {
    var unpackedTransaction = helper.verifyAndUnpackMessage(message, transactionIdCache, Transaction.class);

    if(unpackedTransaction.isPresent()) {
      try {
        transactionManager.storeTransaction(unpackedTransaction.get());
      } catch (BlockchainException e) {
        LOG.error("Invalid Transaction-Message received: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.FRESH_TRANSACTION;
  }
}
