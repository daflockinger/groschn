package com.flockinger.groschn.messaging.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.commons.compress.CompressedEntity;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.Test;


public class MessagingContextTest {

  private Compressor compressor = mock(Compressor.class);
  private BlockSerializer serializer = mock(BlockSerializer.class);
  private Executor executorMock = mock(Executor.class);
  private ClusterCommunicationService clusterComMock = mock(ClusterCommunicationService.class);

  private MessagingContext utils =  new MessagingContext(compressor, executorMock,clusterComMock);

  @Test
  public void testExtractPayload_withValidPayload_shouldExtract() {
    TestBlock freshBlock = new TestBlock();
    when(compressor.decompress(any(), any(Integer.class), any(Class.class))).thenReturn(Optional.ofNullable(freshBlock));
    
    Optional<TestBlock> extractedBlock = utils.extractPayload(validMessage(), TestBlock.class);
    assertTrue("verify extracted block exists", extractedBlock.isPresent());
    assertEquals("verify extracted block is correct", freshBlock, extractedBlock.get());
  }
  
  @Test
  public void testExtractPayload_withDecompressionFailed_shouldExtract() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.empty());
    
    Optional<TestBlock> extractedBlock = utils.extractPayload(validMessage(), TestBlock.class);
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

  @Test
  public void testSerializer_shouldReturnSerializer() {
    when(compressor.serializer()).thenReturn(serializer);
    assertEquals("verify that utils returns the correct serializer", serializer, utils.serializer());
  }

  @Test
  public void testExecutor_shouldReturnSerializer() {
    assertEquals("verify that utils returns the correct executor", executorMock, utils.executor());
  }


  @Test
  public void testClusterCommunicationService_shouldReturnSerializer() {
    assertEquals("verify that utils returns the correct ClusterCommunicationService",  clusterComMock, utils.clusterCommunicationService());
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
