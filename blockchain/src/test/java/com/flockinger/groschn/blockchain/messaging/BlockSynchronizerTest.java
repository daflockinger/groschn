package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.validation.BlockValidationException;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes = {BlockSynchronizer.class})
public class BlockSynchronizerTest extends BaseCachingTest {
  
  @MockBean
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("SyncBlockId_Cache")
  private Cache<String, String> syncBlockIdCache;
  @MockBean
  private SyncInquirer inquirer;
  
  @Autowired
  private BlockSynchronizer synchronizer;
  
  @Before
  public void setup() {
    syncBlockIdCache.invalidateAll();
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withValidFreshSyncRequestLastResponseSignalsEnd_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(true,153l));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    synchronizer.synchronize(123l);
    
    var blockStoreCaptor = ArgumentCaptor.forClass(Block.class);
    verify(blockService, times(10 * 4)).saveInBlockchain(blockStoreCaptor.capture());
    List<Block> toStoreBlocks = blockStoreCaptor.getAllValues();
    assertEquals("verify 1st stored block position is correct", 123l, toStoreBlocks.get(0).getPosition().longValue());
    assertEquals("verify 2nd stored block position is correct", 124l, toStoreBlocks.get(1).getPosition().longValue());
    assertEquals("verify 3rd stored block position is correct", 125l, toStoreBlocks.get(2).getPosition().longValue());
    assertEquals("verify 4th stored block position is correct", 126l, toStoreBlocks.get(3).getPosition().longValue());
    assertEquals("verify 5th stored block position is correct", 127l, toStoreBlocks.get(4).getPosition().longValue());
    assertEquals("verify 6th stored block position is correct", 128l, toStoreBlocks.get(5).getPosition().longValue());
    assertEquals("verify 7th stored block position is correct", 129l, toStoreBlocks.get(6).getPosition().longValue());
    assertEquals("verify 8th stored block position is correct", 130l, toStoreBlocks.get(7).getPosition().longValue());
    assertEquals("verify 9th stored block position is correct", 131l, toStoreBlocks.get(8).getPosition().longValue());
    assertEquals("verify 10th stored block position is correct", 132l, toStoreBlocks.get(9).getPosition().longValue());
    ArgumentCaptor<SyncBatchRequest> batchCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer, times(1 * 4)).fetchNextBatch(batchCaptor.capture(), any(Class.class));
    var batchRequests = batchCaptor.getAllValues();
    assertEquals("verify first requests batch size", 10, batchRequests.get(0).getBatchSize());
    assertEquals("verify first requests fetch retry count", 3, batchRequests.get(0).getMaxFetchRetries());
    assertEquals("verify first requests ideal node count", 3, batchRequests.get(0).getIdealReceiveNodeCount());
    assertEquals("verify first requests correct start position", 123l, batchRequests.get(0).getFromPosition());
    assertEquals("verify first requests correct target topic", MainTopics.SYNC_BLOCKCHAIN, batchRequests.get(0).getTopic());
    assertEquals("verify second request start position", 133l, batchRequests.get(1).getFromPosition());
    assertEquals("verify third request start position", 143l, batchRequests.get(2).getFromPosition());
    assertEquals("verify fourth request start position", 153l, batchRequests.get(3).getFromPosition());
    assertEquals("verify after processing syncing status is on Done", SyncStatus.DONE.name(),
        syncBlockIdCache.getIfPresent(SyncStatus.SYNC_STATUS_CACHE_KEY));
  }
  
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withValidLastRespnseReturnsNoBlocks_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(false,153l,new ArrayList<>()));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    synchronizer.synchronize(123l);
    
    verify(blockService, times(10 * 3)).saveInBlockchain(any());
    verify(inquirer, times(1 * 4)).fetchNextBatch(any(), any());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withValidLastRespnseReturnsNullBlocks_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(false,153l, null));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    synchronizer.synchronize(123l);
    
    verify(blockService, times(10 * 3)).saveInBlockchain(any());
    verify(inquirer, times(1 * 4)).fetchNextBatch(any(), any());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withLastTotallyEmptyInsteadResponseFromInquirer_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(Optional.empty());
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    synchronizer.synchronize(123l);
    
    verify(blockService, times(10 * 3)).saveInBlockchain(any());
    verify(inquirer, times(1 * 4)).fetchNextBatch(any(), any());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withOnlyOneEmptyResponse_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(true,123l,new ArrayList<>()));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    synchronizer.synchronize(123l);
    
    verify(blockService, times(0)).saveInBlockchain(any());
    verify(inquirer, times(1)).fetchNextBatch(any(), any());
  }
  
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withStorageFailingInTheMiddle_shouldRetryThatOne() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,123l)).thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l))
    .thenReturn(getFakeResponse(true,153l));
    when(blockService.saveInBlockchain(any())).thenThrow(BlockValidationException.class).thenReturn(new StoredBlock());
    
    synchronizer.synchronize(123l);

    verify(blockService, times(41)).saveInBlockchain(any());
    var batchCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer, times(1 * 5)).fetchNextBatch(batchCaptor.capture(), any());
    var batchRequests = batchCaptor.getAllValues();
    assertEquals("verify first requests batch size", 10, batchRequests.get(0).getBatchSize());
    assertEquals("verify first requests fetch retry count", 3, batchRequests.get(0).getMaxFetchRetries());
    assertEquals("verify first requests ideal node count", 3, batchRequests.get(0).getIdealReceiveNodeCount());
    assertEquals("verify first requests correct start position", 123l, batchRequests.get(0).getFromPosition());
    assertEquals("verify first requests correct target topic", MainTopics.SYNC_BLOCKCHAIN, batchRequests.get(0).getTopic());
    assertEquals("verify first retry request start position", 123l, batchRequests.get(1).getFromPosition());
    assertEquals("verify second request start position", 133l, batchRequests.get(2).getFromPosition());
    assertEquals("verify third request start position", 143l, batchRequests.get(3).getFromPosition());
    assertEquals("verify fourth request start position", 153l, batchRequests.get(4).getFromPosition());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withStorageFailingAgainAndAgain_shouldGiveUpWithoutException() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l));
    when(blockService.saveInBlockchain(any())).thenThrow(BlockValidationException.class);
    
    synchronizer.synchronize(123l);

    verify(blockService, times(2)).saveInBlockchain(any());
    verify(inquirer, times(2)).fetchNextBatch(any(), any());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_whenItsCurrentlyAlreadySyncing_shouldDoNothing() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(true,153l));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    syncBlockIdCache.put(SyncStatus.SYNC_STATUS_CACHE_KEY, SyncStatus.IN_PROGRESS.name());
    
    synchronizer.synchronize(123l);
    
    verify(blockService, times(0)).saveInBlockchain(any());
    verify(inquirer, times(0)).fetchNextBatch(any(), any(Class.class));
  }
 
  
  Optional<SyncResponse<Block>> getFakeResponse(boolean isLast, long startPos) {
    var blocks = new ArrayList<Block>();
    var block1 = new Block();
    block1.setPosition(123l);
    var block2 = new Block();
    block2.setPosition(124l);
    var block3 = new Block();
    block3.setPosition(125l);
    var block4 = new Block();
    block4.setPosition(126l);
    var block5 = new Block();
    block5.setPosition(127l);
    var block6 = new Block();
    block6.setPosition(128l);
    var block7 = new Block();
    block7.setPosition(129l);
    var block8 = new Block();
    block8.setPosition(130l);
    var block9 = new Block();
    block9.setPosition(131l);
    var block10 = new Block();
    block10.setPosition(132l);
    blocks.addAll(ImmutableList.of(block3, block2, block4, block1, block7));
    blocks.addAll(ImmutableList.of(block8, block5, block10, block6, block9));
    return getFakeResponse(isLast, startPos, blocks);
  }

  Optional<SyncResponse<Block>> getFakeResponse(boolean isLast, long startPos, List<Block> blocks) {
    var response = new SyncResponse<Block>();
    response.setEntities(blocks);
    response.setLastPositionReached(isLast);
    response.setStartingPosition(startPos);
    return Optional.ofNullable(response);
  }
  
  
}
