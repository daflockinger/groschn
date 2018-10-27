package com.flockinger.groschn.blockchain.transaction.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.AbstractMessageListener;
import com.github.benmanes.caffeine.cache.Cache;

@Component
public class TransactionPoolListener extends AbstractMessageListener<Transaction> {
  
  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  @Qualifier("TransactionId_Cache")
  private Cache<String, String> transactionIdCache;
  
  @Override
  protected void handleMessage(Transaction transaction) {
    transactionManager.storeTransaction(transaction);
  }
  
  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.FRESH_TRANSACTION;
  }
  @Override
  protected Cache<String, String> getCache() {
    return transactionIdCache;
  }
  @Override
  protected Class<Transaction> messageBodyType() {
    return Transaction.class;
  }
}
