package com.flockinger.groschn.blockchain.messaging.sync.impl;

import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.sync.GeneralMessageResponder;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class BlockSyncStatusResponder extends GeneralMessageResponder {

  

  //TODO implement
  @Override
  protected Message<MessagePayload> createResponse(SyncRequest request) {
    return null;
  }
  
 

  @Override
  protected Cache<String, String> getCache() {
    // TODO Auto-generated method stub
    return null;
  }

  
  @Override
  public MainTopics getSubscribedTopic() {
    return null;
  }
}
