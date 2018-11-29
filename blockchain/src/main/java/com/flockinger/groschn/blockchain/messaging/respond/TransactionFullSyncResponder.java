package com.flockinger.groschn.blockchain.messaging.respond;

import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("TransactionFullSyncResponder")
public class TransactionFullSyncResponder implements MessageResponder<MessagePayload>{

  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  @Qualifier("SyncTransactionId_Cache")
  private Cache<String, String> syncTransactionIdCache;
  @Autowired
  private MessagePackageHelper helper;
  
  @Value("${atomix.node-id}")
  private String nodeId;

  @Override
  public Message<MessagePayload> respond(Message<MessagePayload> request) {
    Message<MessagePayload> responseMessage = new Message<>();
    var syncRequest = helper.verifyAndUnpackRequest(request, syncTransactionIdCache);
    if(syncRequest.isPresent()) {
      var syncResponse = createResponse(syncRequest.get());
      responseMessage = helper.packageResponse(syncResponse, nodeId);
    }
    return responseMessage;
  }

  protected SyncResponse<Transaction> createResponse(SyncRequest request) {
    int page = request.getStartingPosition().intValue() - 1;
    int size = request.getStartingPosition().intValue() * request.getRequestPackageSize().intValue();
    List<Transaction> transactions = transactionManager.fetchTransactionsPaginated(page, size);
    SyncResponse<Transaction> response = new SyncResponse<>();
    response.setEntities(transactions);
    response.setLastPositionReached(transactions.size() < request.getRequestPackageSize());
    response.setStartingPosition(request.getStartingPosition());
    
    return response;
  }

  @Override
  public MainTopics getSubscribedTopic() {
    return MainTopics.SYNC_TRANSACTIONS;
  }
}
