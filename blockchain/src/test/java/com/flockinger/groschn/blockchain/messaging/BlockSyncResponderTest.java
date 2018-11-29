package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.respond.BlockSyncResponder;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.RequestHeader;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = {BlockSyncResponder.class})
public class BlockSyncResponderTest extends BaseCachingTest {

  @MockBean
  private BlockStorageService blockService;
  @MockBean
  private MessagePackageHelper helperMock;

  @MockBean
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
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(123l);
    request.setWantedHeaders(headers(12));
    request.setRequestPackageSize(12l);

    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(12));
    when(helperMock.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helperMock.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helperMock).packageResponse(responseCaptor.capture(),anyString());
    
    SyncResponse response = responseCaptor.getValue();
    assertEquals("verify that response starting position is correct", 123l, response.getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 12l, response.getEntities().size());
    assertTrue("verify that response entity is correct class", Block.class.isInstance(response.getEntities().get(0)));
    assertEquals("verify that response is not the last sync", false, response.isLastPositionReached());
  }
  
  
  @Test
  public void testRespond_withValidRequestAndLastBlocks_shouldRespondEndOfSyncing() {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(999l);
    request.setWantedHeaders(headers(3));
    request.setRequestPackageSize(10l);
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    when(helperMock.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helperMock.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helperMock).packageResponse(responseCaptor.capture(),anyString());

    SyncResponse response = responseCaptor.getValue();
    assertEquals("verify that response starting position is correct", 999l, response.getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 3l, response.getEntities().size());
    assertEquals("verify that response is not the last sync", true, response.isLastPositionReached());
  }
  
  @Test
  public void testRespond_withValidNoMatchingBlockHashes_shouldRespondEmpty() {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(123l);
    request.setWantedHeaders(headers(12));
    request.setRequestPackageSize(12l);
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(evilBlocks(12));
    when(helperMock.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helperMock.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helperMock).packageResponse(responseCaptor.capture(),anyString());

    SyncResponse response = responseCaptor.getValue();
    assertEquals("verify that response starting position is correct", 123l,
        response.getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 0l, response.getEntities().size());
    assertEquals("verify that response is not the last sync", false, response.isLastPositionReached());
  }
  
  @Test
  public void testRespond_withOnlySomeMatchingBlockHashes_shouldRespondEmpty() {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(123l);
    request.setWantedHeaders(headers(12));
    request.setRequestPackageSize(12l);
    var notNiceBlocks = someBlocks(12);
    notNiceBlocks.get(3).setHash("evil3");
    notNiceBlocks.get(5).setPosition(300000l);
    notNiceBlocks.get(7).setHash("hash30");
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(notNiceBlocks);
    when(helperMock.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helperMock.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helperMock).packageResponse(responseCaptor.capture(),anyString());

    SyncResponse response = responseCaptor.getValue();
    assertEquals("verify that response starting position is correct", 123l, response.getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 0l, response.getEntities().size());
    assertEquals("verify that response is not the last sync", false, response.isLastPositionReached());
  }

  
  @Test
  public void testRespond_withRequestWithoutWantedHeaders_shouldRespondEmpty() {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(2l);
    request.setWantedHeaders(null);
    request.setRequestPackageSize(10l);
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    when(helperMock.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helperMock.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helperMock).packageResponse(responseCaptor.capture(),anyString());

    SyncResponse response = responseCaptor.getValue();
    assertTrue("verify response entities are empty", response.getEntities().isEmpty());
  }

  @Test
  public void testGetSubscribedTopic_shouldReturnCorrect() {
    assertEquals("verify correct set topic", MainTopics.SYNC_BLOCKCHAIN, responder.getSubscribedTopic());
  }

  
  private List<Block> someBlocks(int size) {
    return LongStream.range(0, size).mapToObj(count -> {
      var block = new Block(); 
      block.setPosition(count);
      block.setHash("hash" + count);
      return block;
      }).collect(Collectors.toList());
  }
  
  private List<Block> evilBlocks(int size) {
    return LongStream.range(0, size).mapToObj(count -> {
      var block = new Block(); 
      block.setPosition(count);
      block.setHash("evil" + count);
      return block;
      }).collect(Collectors.toList());
  }
  
  private List<RequestHeader> headers(int size) {
    return LongStream.range(0, size).mapToObj(count -> {
      var header = new RequestHeader();
      header.setPosition(count);
      header.setHash("hAsH" + count);
      return header;
      }).collect(Collectors.toList());
  }
}
