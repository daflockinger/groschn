package com.flockinger.groschn.blockchain.messaging;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.messaging.model.Message;


@Component
public class MessageReceiverUtils {
  
  @Autowired
  private CompressionUtils compressor;
  
  private final Validator validator;
  
  public MessageReceiverUtils() {
    final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }
  
  public void assertMessage(Message<MessagePayload> message) {
    var possibleErrors = validator.validate(message);
    if(!possibleErrors.isEmpty()) {
      var errorMessage = possibleErrors.stream()
          .map(this::errorToString).collect(Collectors.joining("\n"));
      throw new ReceivedMessageInvalidException(errorMessage);
    }
  }
  
  private String errorToString(ConstraintViolation<Message<MessagePayload>> error) {
    StringBuilder errorMessage = new StringBuilder();
    errorMessage.append(error.getPropertyPath() + " " + error.getMessage());
    
    if(isNotEmptyOrNull(error.getInvalidValue())) {
      errorMessage.append(" but was: " + error.getInvalidValue());
    }
    errorMessage.append("\n");
    return errorMessage.toString();
  }
  
  private boolean isNotEmptyOrNull(Object value) {
    return (value instanceof String && StringUtils.isNotEmpty((String)value)) 
        || (!(value instanceof String) && value != null);
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
}
