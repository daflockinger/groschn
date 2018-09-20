package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.messaging.model.CompressedEntity;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MessagingUtils.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, 
  MockitoTestExecutionListener.class, ResetMocksTestExecutionListener.class})
public class MessagingUtilsTest {

  @MockBean
  private CompressionUtils compressor;
  
  @Autowired
  private MessagingUtils utils;
  
 
  @Test
  public void testAssertMessage_withValidBlockAndData_shouldDoNothing() {
    Message<MessagePayload> message = validMessage();
    
    utils.assertEntity(message);
  }

  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullId_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.setId(null);
    
    utils.assertEntity(message);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullTimestamp_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.setTimestamp(null);
    
    utils.assertEntity(message);
  }  
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullPayload_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.setPayload(null);
    
    utils.assertEntity(message);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullBlockMessageEntity_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    MessagePayload bm = message.getPayload();
    bm.setEntity(null);
    message.setPayload(bm);
    
    utils.assertEntity(message);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullBlockMessageSenderId_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    MessagePayload bm = message.getPayload();
    bm.setSenderId(null);
    message.setPayload(bm);
    
    utils.assertEntity(message);;
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withZeroCompressedOriginalSize_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.getPayload().getEntity().originalSize(0);
    
    utils.assertEntity(message);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withEmptyCompressedEntity_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.getPayload().getEntity().entity(new byte[0]);
    
    utils.assertEntity(message);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testAssertMessage_withNullCompressedEntity_shouldThrowException() {
    Message<MessagePayload> message = validMessage();
    message.getPayload().getEntity().entity(null);
    
    utils.assertEntity(message);
  }
  
  
  @Test
  public void testExtractPayload_withValidPayload_shouldExtract() {
    Block freshBlock = new Block();
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.ofNullable(freshBlock));
    
    Optional<Block> extractedBlock = utils.extractPayload(validMessage(), Block.class);
    assertTrue("verify extracted block exists", extractedBlock.isPresent());
    assertEquals("verify extracted block is correct", freshBlock, extractedBlock.get());
  }
  
  @Test
  public void testExtractPayload_withDecompressionFailed_shouldExtract() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.empty());
    
    Optional<Block> extractedBlock = utils.extractPayload(validMessage(), Block.class);
    assertFalse("verify extracted block exists", extractedBlock.isPresent());
  }
  
  @Test
  public void testPackageMessage_withValidThing_shouldPackageCorrectly() {
    SyncRequest request = new SyncRequest();
    when(compressor.compress(any())).thenReturn(CompressedEntity.build().entity(new byte[10]).originalSize(2));
    
    Message<MessagePayload> message = utils.packageMessage(request, "Spock");
    
    assertNotNull("verify received message is not null", message);
    assertNotNull("verify message has an id", message.getId());
    assertNotNull("verify message has a timestamp", message.getTimestamp());
    assertNotNull("verify message has a payload", message.getPayload());
    assertNotNull("verify message has a senderId", message.getPayload().getSenderId());
    assertNotNull("verify message has a compressed entity", message.getPayload().getEntity());
    assertNotNull("verify message has a compressed entity data", message.getPayload().getEntity().getEntity());
    assertTrue("verify message has some compressed entity size", message.getPayload().getEntity().getOriginalSize() > 0);
    
    verify(compressor).compress(any());
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
