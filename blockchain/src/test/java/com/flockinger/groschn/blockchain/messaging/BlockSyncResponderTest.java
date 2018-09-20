package com.flockinger.groschn.blockchain.messaging;

import static com.flockinger.groschn.blockchain.TestDataFactory.validMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSyncResponder;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.serialize.impl.FstSerializer;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.CompressedEntity;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.ImmutableList;


@ContextConfiguration(classes = {BlockSyncResponder.class, CompressionUtils.class, MessagingUtils.class, FstSerializer.class})
public class BlockSyncResponderTest extends BaseCachingTest {
  
  @Autowired
  private CompressionUtils compressor;
  @MockBean
  private NetworkStatistics networkStatistics;
  @MockBean
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("SyncBlockId_Cache")
  private Cache<String, String> syncBlockIdCache;
  
  @Autowired 
  private BlockSyncResponder responder;
  
  @Before
  public void setup() {
    syncBlockIdCache.cleanUp();
  }
  
  @Test
  public void testRespond_withValidRequestAndEnoughBlocks_shouldRespondNormal() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(123l);
    request.setRequestPackageSize(12l);
    message.getPayload().setEntity(compressor.compress(request));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(12));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    
    var responseMessage = responder.respond(message);
    
    assertTrue("verify response exists", responseMessage.isPresent());
    assertNotNull("verify response-message has a timestamp", responseMessage.get().getTimestamp());
    assertNotNull("verify response-message has a non null payload", responseMessage.get().getPayload());
    assertEquals("verify response-message has a valida sender id", "groschn-master-123", 
        responseMessage.get().getPayload().getSenderId());
    assertNotNull("verify response-message has an payload entity", responseMessage.get().getPayload().getEntity());
    CompressedEntity entity = responseMessage.get().getPayload().getEntity();
    assertTrue("verify that compressed entity is not empty", entity.getEntity().length > 0);
    assertTrue("verify that compressed entity has an original size", entity.getOriginalSize() > 0);
    
    var response = compressor.decompress(entity.getEntity(), entity.getOriginalSize(), SyncResponse.class);
    assertTrue("verify that response is there", response.isPresent());
    assertEquals("verify that response starting position is correct", 123l, 
        response.get().getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 12l, response.get().getEntities().size());
    assertTrue("verify that response entity is correct class", Block.class.isInstance(response.get().getEntities().get(0)));
    assertEquals("verify that response is not the last sync", false, response.get().isLastPositionReached());
  }
  
  
  @Test
  public void testRespond_withValidRequestAndLastBlocks_shouldRespondEndOfSyncing() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(999l);
    request.setRequestPackageSize(10l);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    
    var responseMessage = responder.respond(message);
    
    assertTrue("verify response exists", responseMessage.isPresent());
    assertNotNull("verify response-message has a timestamp", responseMessage.get().getTimestamp());
    assertNotNull("verify response-message has a non null payload", responseMessage.get().getPayload());
    assertEquals("verify response-message has a valida sender id", "groschn-master-123", 
        responseMessage.get().getPayload().getSenderId());
    assertNotNull("verify response-message has an payload entity", responseMessage.get().getPayload().getEntity());
    CompressedEntity entity = responseMessage.get().getPayload().getEntity();
    assertTrue("verify that compressed entity is not empty", entity.getEntity().length > 0);
    assertTrue("verify that compressed entity has an original size", entity.getOriginalSize() > 0);
    
    var response = compressor.decompress(entity.getEntity(), entity.getOriginalSize(), SyncResponse.class);
    assertTrue("verify that response is there", response.isPresent());
    assertEquals("verify that response starting position is correct", 999l, 
        response.get().getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 3l, response.get().getEntities().size());
    assertEquals("verify that response is not the last sync", true, response.get().isLastPositionReached());
  }
  
  @Test
  public void testRespond_withInvalidRequestMessage_shouldRespondEmpty() {
    var message = validMessage();
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
  
  @Test
  public void testRespond_withRequestWithoutStartingPoint_shouldRespondEmpty() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(null);
    request.setRequestPackageSize(10l);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
  
  @Test
  public void testRespond_withRequestWithoutBatchSize_shouldRespondEmpty() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(2l);
    request.setRequestPackageSize(null);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
  
  
  @Test
  public void testRespond_withMessageAlreadyReceived_shouldRespondEmpty() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(999l);
    request.setRequestPackageSize(10l);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    
    responder.respond(message);
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
  
  @Test
  public void testRespond_withRequesterNotExisting_shouldRespondEmpty() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(999l);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("yen-master-123", "groschn-master"));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
  
  private List<Block> someBlocks(int size) {
    return IntStream.range(0, size).mapToObj(count -> new Block()).collect(Collectors.toList());
  }
}
