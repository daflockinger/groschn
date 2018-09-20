package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.GeneralMessageResponder;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.github.benmanes.caffeine.cache.Cache;

@Service("TransactionFullSyncResponder")
public class TransactionFullSyncResponder extends GeneralMessageResponder {
  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  private MessagingUtils messageUtils;
  @Autowired
  @Qualifier("SyncTransactionId_Cache")
  private Cache<String, String> syncTransactionIdCache;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  protected Message<MessagePayload> createResponse(SyncRequest request) {
    int page = request.getStartingPosition().intValue() - 1;
    int size = request.getStartingPosition().intValue() * request.getRequestPackageSize().intValue();
    List<Transaction> transactions = transactionManager.fetchTransactionsPaginated(page, size);
    SyncResponse<Transaction> response = new SyncResponse<>();
    response.setEntities(transactions);
    response.setLastPositionReached(transactions.size() < request.getRequestPackageSize());
    response.setStartingPosition(request.getStartingPosition());
    
    return messageUtils.packageMessage(response, nodeId);
  }
  
  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.SYNC_TRANSACTIONS;
  }
  
  @Override
  protected Cache<String, String> getCache() {
    return syncTransactionIdCache;
  }
}
