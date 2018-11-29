package com.flockinger.groschn.messaging.util;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.compress.CompressedEntity;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.springframework.stereotype.Component;


@Component
public class MessagingContext {

  private final Compressor compressor;
  private final BlockSerializer serializer;
  private final Validator validator;
  private final Executor executor;
  private final  ClusterCommunicationService clusterCommunicationService;
  
  public MessagingContext(
      Compressor compressor,
      BlockSerializer serializer,
      Executor executor,
      ClusterCommunicationService clusterCommunicationService
  ) {
    this.compressor = compressor;
    this.serializer = serializer;
    this.executor = executor;
    this.clusterCommunicationService = clusterCommunicationService;
    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  public <T extends Hashable> Optional<T> extractPayload(Message<MessagePayload> message, Class<T> type) {
    Optional<MessagePayload> receivedBlockMessage = Optional.empty();
    receivedBlockMessage = Optional.ofNullable(message.getPayload());
    return receivedBlockMessage.stream()
        .map(MessagePayload::getEntity)
        .map(entity -> decompressEntity(entity, type)).filter(Optional::isPresent)
        .map(Optional::get).findFirst();
  }
  
  private <T extends Hashable> Optional<T> decompressEntity(CompressedEntity compressedBlock, Class<T> type) {
    return compressor.decompress(compressedBlock.getEntity(), compressedBlock.getOriginalSize(), type);
  }
  
  public <T extends Hashable> Message<MessagePayload> packageMessage(T uncompressedEntity, String senderId) {
    Message<MessagePayload> message = new Message<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(new Date().getTime());
    MessagePayload payload = new MessagePayload();
    payload.setSenderId(senderId);
    payload.setEntity(compressor.compress(uncompressedEntity));
    message.setPayload(payload);
    return message;
  }

  public BlockSerializer serializer() {
    return serializer;
  }

  public Executor executor() {
    return  executor;
  }

  public  ClusterCommunicationService clusterCommunicationService() {
    return clusterCommunicationService;
  }
}
