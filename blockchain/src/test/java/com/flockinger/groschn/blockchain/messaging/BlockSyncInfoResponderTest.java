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
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.respond.BlockSyncInfoResponder;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {BlockSyncInfoResponder.class})
public class BlockSyncInfoResponderTest extends BaseCachingTest {

  @MockBean
  private BlockStorageService blockService;
  @MockBean
  @Qualifier("SyncBlockInfoId_Cache")
  private Cache<String, String> syncBlockIdCache;
  @MockBean
  private MessagePackageHelper helper;

  @Autowired 
  private BlockSyncInfoResponder responder;

  
  @Test
  public void testRespond_withValidRequestAndEnoughBlockInfos_shouldRespondNormal() {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(123l);
    request.setRequestPackageSize(12l);
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(12));
    when(blockService.getLatestBlock()).thenReturn(someBlocks(12).get(11));
    when(helper.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helper.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helper).packageResponse(responseCaptor.capture(),anyString());

    SyncResponse response = responseCaptor.getValue();
    assertEquals("verify that response starting position is correct", 123l, response.getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 12l, response.getEntities().size());
    assertTrue("verify that response entity is correct class", BlockInfo.class.isInstance(response.getEntities().get(0)));
    BlockInfo firstInfo = (BlockInfo)response.getEntities().get(0);
    assertTrue("verify correct first response entity hash", firstInfo.getBlockHash().startsWith("1"));
    assertEquals("verify correct first response entity position", 1, firstInfo.getPosition().longValue());
    BlockInfo secondInfo = (BlockInfo)response.getEntities().get(1);
    assertTrue("verify correct first response entity hash", secondInfo.getBlockHash().startsWith("2"));
    assertEquals("verify correct first response entity position", 2, secondInfo.getPosition().longValue());
    assertEquals("verify correct last block position", 12l, response.getLastPosition().longValue());
    assertEquals("verify correct node it is in response", "groschn-master-123", response.getNodeId());
  }
  
  
  @Test
  public void testRespond_withValidRequestAndLastBlockInfos_shouldRespondEndOfSyncing() {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(999l);
    request.setRequestPackageSize(10l);
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(someBlocks(3));
    when(blockService.getLatestBlock()).thenReturn(someBlocks(3).get(2));
    when(helper.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helper.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helper).packageResponse(responseCaptor.capture(),anyString());

    SyncResponse response = responseCaptor.getValue();
    assertEquals("verify that response starting position is correct", 999l, response.getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 3l, response.getEntities().size());
    assertEquals("verify correct last block position", 3l, response.getLastPosition().longValue());
    assertEquals("verify correct node it is in response", "groschn-master-123", response.getNodeId());
  }

  @Test
  public void testGetSubscribedTopic_shouldReturnCorrect() {
    assertEquals("verify correct set topic", MainTopics.BLOCK_INFO, responder.getSubscribedTopic());
  }
  
  private List<Block> someBlocks(int size) {
    return IntStream.range(1, size + 1).mapToObj(this::newBlock).collect(Collectors.toList());
  }
  
  private Block newBlock(int count) {
    var block = new Block();
    block.setPosition(Integer.toUnsignedLong(count));
    block.setHash(count + UUID.randomUUID().toString());
    return block;
  }
}
