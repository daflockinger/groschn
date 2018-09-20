package com.flockinger.groschn.blockchain.messaging.sync;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.github.benmanes.caffeine.cache.Cache;

public abstract class GeneralMessageResponder implements MessageResponder<MessagePayload> {
  @Autowired
  private NetworkStatistics networkStatistics;
  @Autowired
  private MessagingUtils messageUtils;
  private final static Logger LOG = LoggerFactory.getLogger(MessageResponder.class);

  @Override
  public abstract MainTopics getSubscribedTopic();
  
  protected abstract Cache<String, String> getCache();
  
  protected abstract Message<MessagePayload> createResponse(SyncRequest request);

  
  @Override
  public Optional<Message<MessagePayload>> respond(Message<MessagePayload> request) {
    Optional<Message<MessagePayload>> response = Optional.empty();
    try {
      messageUtils.assertEntity(request);
      isMessageFromALegitSender(request.getPayload().getSenderId());
      assertMessageIsNew(request.getId());
      Optional<SyncRequest> syncRequest = messageUtils.extractPayload(request, SyncRequest.class);
      if (syncRequest.isPresent()) {
        messageUtils.assertEntity(syncRequest.get());
        response = Optional.ofNullable(createResponse(syncRequest.get()));
      }
    } catch (BlockchainException e) {
      LOG.error("Invalid Syncing-Request received: " + e.getMessage(), e);
    }
    return response;
  }
  
  private void assertMessageIsNew(String messageId) {
    Optional<String> existingKey = Optional.ofNullable(getCache().getIfPresent(messageId));
    if(!existingKey.isPresent()) {
      getCache().put(messageId, messageId);
    } else {
      throw new ReceivedMessageInvalidException("Syncing-Request was already received with ID: " + messageId);
    }
  }
  private void isMessageFromALegitSender(String senderId) {
    boolean doesSenderExist = networkStatistics.activeNodeIds().stream()
        .anyMatch(nodeId -> StringUtils.equals(nodeId, senderId));
    if(!doesSenderExist) {
      throw new ReceivedMessageInvalidException("Sender is not existing in network with ID: " + senderId);
    }
  }
}
