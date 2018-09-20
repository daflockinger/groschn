package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockSynchronizationException;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSyncDeterminator;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockSyncDeterminator.class})
@SuppressWarnings("unchecked")
public class BlockSyncDeterminatorTest {
  
  @MockBean
  private BlockSynchronizer synchronizer;
  @MockBean
  private BlockStorageService blockService;
  @MockBean
  private SyncInquirer inquirer;
  
  @Autowired
  private BlockSyncDeterminator determinator;
  
  
  @Test
  public void testDetermineAndSync_withHugeGapBetweenLatestStoredAndRealLatestBlock_shouldDetermineAndCallSync() {
    var fullFakeBlocks = getFakeBlocks(200);
    var storedFakeBlocks = fullFakeBlocks.subList(100, 200);
    var fakeInfos = fullFakeBlocks.subList(1, 101).stream().map(this::mapToInfo).collect(Collectors.toList());
    Collections.shuffle(fakeInfos);
    
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class)))
    .thenReturn(response(fakeInfos));
    
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(storedFakeBlocks);
    when(blockService.getLatestBlock()).thenReturn(storedFakeBlocks.get(0));
    
    determinator.determineAndSync();
    
    var batchRequestCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer).fetchNextBatch(batchRequestCaptor.capture(), any(Class.class));
    var batchInfoRequest = batchRequestCaptor.getValue();
    assertEquals("verify that blockInfo request start position is equal to last block position", 
        batchInfoRequest.getFromPosition(), storedFakeBlocks.get(0).getPosition().longValue());
    assertEquals("verify that the target topic is correct", MainTopics.BLOCK_INFO, batchInfoRequest.getTopic());
    
    var syncPositionCaptor = ArgumentCaptor.forClass(Long.class);
    verify(synchronizer).synchronize(syncPositionCaptor.capture());
    var syncPosition = syncPositionCaptor.getValue();
    assertNotNull("verify chosen start position is not null", syncPosition);
    assertEquals("verify chosen sync start position is correct", 101l, syncPosition.longValue());
    var removeFromCaptor = ArgumentCaptor.forClass(Long.class);
    verify(blockService).removeBlocks(removeFromCaptor.capture());
    var removeFrom = removeFromCaptor.getValue();
    assertNotNull("verify remove position is not null", removeFrom);
    assertEquals("verify remove position is correct", 101l, removeFrom.longValue());
  }
  
  @Test
  public void testDetermineAndSync_withFreshNodeSyncing_shouldDetermineAndCallSync() {
    var fullFakeBlocks = getFakeBlocks(101);
    var storedFakeBlocks = fullFakeBlocks.subList(100, 101);
    var fakeInfos = fullFakeBlocks.subList(1, 101).stream().map(this::mapToInfo).collect(Collectors.toList());
    Collections.shuffle(fakeInfos);
    
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class)))
    .thenReturn(response(fakeInfos));
    
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(storedFakeBlocks);
    when(blockService.getLatestBlock()).thenReturn(storedFakeBlocks.get(0));
    
    determinator.determineAndSync();
    
    var batchRequestCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer).fetchNextBatch(batchRequestCaptor.capture(), any(Class.class));
    var batchInfoRequest = batchRequestCaptor.getValue();
    assertEquals("verify that blockInfo request start position is equal to last block position", 
        batchInfoRequest.getFromPosition(), storedFakeBlocks.get(0).getPosition().longValue());
    
    var syncPositionCaptor = ArgumentCaptor.forClass(Long.class);
    verify(synchronizer).synchronize(syncPositionCaptor.capture());
    var syncPosition = syncPositionCaptor.getValue();
    assertNotNull("verify chosen start position is not null", syncPosition);
    assertEquals("verify chosen sync start position is correct", 2l, syncPosition.longValue());
    var removeFromCaptor = ArgumentCaptor.forClass(Long.class);
    verify(blockService).removeBlocks(removeFromCaptor.capture());
    var removeFrom = removeFromCaptor.getValue();
    assertNotNull("verify remove position is not null", removeFrom);
    assertEquals("verify remove position is correct", 2l, removeFrom.longValue());
  }
  
  @Test
  public void testDetermineAndSync_withLast200BlocksWrong_shouldDetermineAndCallSync() {
    var fakeBlocks = getFakeBlocks(400);
    
    var fakeInfos = fakeBlocks.stream().map(this::mapToInfo).collect(Collectors.toList());
    var faultyInfos = getFakeBlocks(400).stream().map(this::mapToInfo).collect(Collectors.toList());
    
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class)))
    .thenReturn(response(subListShuffled(faultyInfos, 0, 100))).thenReturn(response(subListShuffled(faultyInfos, 100, 200)))
    .thenReturn(response(subListShuffled(fakeInfos, 200, 300))).thenReturn(response(subListShuffled(fakeInfos, 300, 400)));
    
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(fakeBlocks.subList(0, 100))
    .thenReturn(fakeBlocks.subList(100, 200)).thenReturn(fakeBlocks.subList(200, 300))
    .thenReturn(fakeBlocks.subList(300, 400));
    when(blockService.getLatestBlock()).thenReturn(fakeBlocks.get(0));
    
    determinator.determineAndSync();
    
    var syncPositionCaptor = ArgumentCaptor.forClass(Long.class);
    verify(synchronizer).synchronize(syncPositionCaptor.capture());
    var syncPosition = syncPositionCaptor.getValue();
    assertNotNull("verify chosen start position is not null", syncPosition);
    assertEquals("verify chosen sync start position is correct", 201l, syncPosition.longValue());
    var removeFromCaptor = ArgumentCaptor.forClass(Long.class);
    verify(blockService).removeBlocks(removeFromCaptor.capture());
    var removeFrom = removeFromCaptor.getValue();
    assertNotNull("verify remove position is not null", removeFrom);
    assertEquals("verify remove position is correct", 201l, removeFrom.longValue());
  }
  
  @Test
  public void testDetermineAndSync_withLast123BlocksWrong_shouldDetermineAndCallSync() {
    var fakeBlocks = getFakeBlocks(400);
    var fakeInfos = fakeBlocks.stream().map(this::mapToInfo).collect(Collectors.toList());
    var faultyInfos = getFakeBlocks(400).stream().map(this::mapToInfo).collect(Collectors.toList());
    var twentyThreeFaulty = faultyInfos.subList(100, 123);
    twentyThreeFaulty.addAll(fakeInfos.subList(123, 200));
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class)))
    .thenReturn(response(subListShuffled(faultyInfos, 0, 100))).thenReturn(response(twentyThreeFaulty))
    .thenReturn(response(subListShuffled(fakeInfos, 200, 300))).thenReturn(response(subListShuffled(fakeInfos, 300, 400)));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(fakeBlocks.subList(0, 100))
    .thenReturn(fakeBlocks.subList(100, 200)).thenReturn(fakeBlocks.subList(200, 300))
    .thenReturn(fakeBlocks.subList(300, 400));
    when(blockService.getLatestBlock()).thenReturn(fakeBlocks.get(0));
    
    determinator.determineAndSync();
    
    var syncPositionCaptor = ArgumentCaptor.forClass(Long.class);
    verify(synchronizer).synchronize(syncPositionCaptor.capture());
    var syncPosition = syncPositionCaptor.getValue();
    assertNotNull("verify chosen start position is not null", syncPosition);
    assertEquals("verify chosen sync start position is correct", 278l, syncPosition.longValue());
    var removeFromCaptor = ArgumentCaptor.forClass(Long.class);
    verify(blockService).removeBlocks(removeFromCaptor.capture());
    var removeFrom = removeFromCaptor.getValue();
    assertNotNull("verify remove position is not null", removeFrom);
    assertEquals("verify remove position is correct", 278l, removeFrom.longValue());
  }
  
  @Test(expected = BlockSynchronizationException.class)
  public void testDetermineAndSync_withAllInfosCompletlyWrong_shouldThrowExceptionShouldNotHappen() {
    var fakeBlocks = getFakeBlocks(400);
    var faultyInfos = getFakeBlocks(400).stream().map(this::mapToInfo).collect(Collectors.toList());
   
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class)))
    .thenReturn(response(subListShuffled(faultyInfos, 0, 100))).thenReturn(response(subListShuffled(faultyInfos, 100, 200)))
    .thenReturn(response(subListShuffled(faultyInfos, 200, 300))).thenReturn(response(subListShuffled(faultyInfos, 300, 400)));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(fakeBlocks.subList(0, 100))
    .thenReturn(fakeBlocks.subList(100, 200)).thenReturn(fakeBlocks.subList(200, 300))
    .thenReturn(fakeBlocks.subList(300, 400));
    when(blockService.getLatestBlock()).thenReturn(fakeBlocks.get(0));
    
    determinator.determineAndSync();
  }
  
  
  private List<Block> getFakeBlocks(int amount) {
    var blocks = new ArrayList<Block>();
    for(long count=amount; count > 0; count--) {
      var block = new Block();
      block.setHash(UUID.randomUUID().toString());
      block.setPosition(count);
      blocks.add(block);
    }
    return blocks;
  }
  
  private BlockInfo mapToInfo(Block block) {
    var info = new BlockInfo();
    info.setBlockHash(block.getHash());
    info.setPosition(block.getPosition());
    return info;
  }
  
  private Optional<SyncResponse<BlockInfo>> response(List<BlockInfo> infos) {
    var infoResponse = new SyncResponse<BlockInfo>();
    infoResponse.setEntities(infos);
    return Optional.ofNullable(infoResponse);
  }
  
  private List<BlockInfo> subListShuffled(List<BlockInfo> infos, int from, int to) {
    var subList = infos.subList(from, to);
    Collections.shuffle(subList);
    return subList;
  }
}
