package com.flockinger.groschn.messaging.inbound;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.exception.BlockchainException;
import com.flockinger.groschn.messaging.exception.ReceivedMessageInvalidException;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.util.BeanValidator;
import com.flockinger.groschn.messaging.util.MessagingContext;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MessagePackageHelper {

  private final NetworkStatistics networkStatistics;
  private final BeanValidator validator;
  private final MessagingContext messageUtils;

  private final static Logger LOG = LoggerFactory.getLogger(MessageResponder.class);

  public MessagePackageHelper(NetworkStatistics networkStatistics, BeanValidator validator, MessagingContext messageUtils) {
    this.networkStatistics = networkStatistics;
    this.messageUtils = messageUtils;
    this.validator = validator;
  }

  public Optional<SyncRequest> verifyAndUnpackRequest(Message<MessagePayload> request, Cache<String, String> cache) {
    Optional<SyncRequest> syncRequest;
    try {
      validateMessage(request, cache);
      syncRequest = messageUtils.extractPayload(request, SyncRequest.class);
      if (syncRequest.isPresent()) {
        validator.assertEntity(syncRequest.get());
      }
    } catch (BlockchainException e) {
      LOG.error("Invalid Syncing-Request received: " + e.getMessage(), e);
      syncRequest = Optional.empty();
    }
    return syncRequest;
  }

  public <T extends Hashable<T>> Optional<T> verifyAndUnpackMessage(Message<MessagePayload> message, Cache<String, String> cache, Class<T> type) {
    Optional<T> unpackedMessage = Optional.empty();
    try {
      validateMessage(message, cache);
      Optional<T> extractedMessage = messageUtils.extractPayload(message, type);
      if (extractedMessage.isPresent()) {
        LOG.info("{} message received.", type.getSimpleName());
        unpackedMessage = extractedMessage;
      }
    } catch (BlockchainException e) {
      LOG.error("Invalid {}-Message received: " + e.getMessage(), e, type.getSimpleName());
      unpackedMessage = Optional.empty();
    }
    return unpackedMessage;
  }

  private void validateMessage(Message<MessagePayload> message,  Cache<String, String> cache) {
    validator.assertEntity(message);
    isMessageFromALegitSender(message.getPayload().getSenderId());
    assertMessageIsNew(message.getId(), cache);
  }

  public <T extends Hashable<T>> Message<MessagePayload> packageResponse(SyncResponse<T> response, String ownNodeId) {
    return messageUtils.packageMessage(response, ownNodeId);
  }

  private void assertMessageIsNew(String messageId, Cache<String, String> cache) {
    Optional<String> existingKey = Optional.ofNullable(cache.getIfPresent(messageId));
    if(!existingKey.isPresent()) {
      cache.put(messageId, messageId);
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
