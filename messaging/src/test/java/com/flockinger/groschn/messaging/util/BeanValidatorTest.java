package com.flockinger.groschn.messaging.util;

import com.flockinger.groschn.commons.compress.CompressedEntity;
import com.flockinger.groschn.messaging.exception.ReceivedMessageInvalidException;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import java.util.UUID;
import org.junit.Test;

public class BeanValidatorTest {

  private BeanValidator validator = new BeanValidator();

  @Test
  public void testAssertMessage_withValidBlockAndData_shouldDoNothing() {
    Message<MessagePayload> message = validMessage();

    validator.assertEntity(message);
  }


  @Test(expected= ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullId_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.setId(null);

    validator.assertEntity(message);
  }

  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullTimestamp_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.setTimestamp(null);

    validator.assertEntity(message);
  }

  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullPayload_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.setPayload(null);

    validator.assertEntity(message);
  }

  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullBlockMessageEntity_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    MessagePayload bm = message.getPayload();
    bm.setEntity(null);
    message.setPayload(bm);

    validator.assertEntity(message);
  }

  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullBlockMessageSenderId_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    MessagePayload bm = message.getPayload();
    bm.setSenderId(null);
    message.setPayload(bm);

    validator.assertEntity(message);;
  }

  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withZeroCompressedOriginalSize_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.getPayload().getEntity().originalSize(0);

    validator.assertEntity(message);
  }

  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withEmptyCompressedEntity_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.getPayload().getEntity().entity(new byte[0]);

    validator.assertEntity(message);
  }

  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullCompressedEntity_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.getPayload().getEntity().entity(null);

    validator.assertEntity(message);
  }

  private Message<MessagePayload> validMessage() {
    Message<MessagePayload> message = new Message<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(1000l);
    MessagePayload blockMessage = new MessagePayload();
    CompressedEntity entity = CompressedEntity.build().originalSize(123).entity(new byte[10]);
    blockMessage.setEntity(entity);
    blockMessage.setSenderId(UUID.randomUUID().toString());
    message.setPayload(blockMessage);
    return message;
  }
}