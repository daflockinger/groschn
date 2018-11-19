package com.flockinger.groschn.blockchain.messaging;

import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.RESTART;
import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.STOP;
import static com.flockinger.groschn.blockchain.TestDataFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.collections4.ListUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;
import com.flockinger.groschn.blockchain.exception.validation.BlockValidationException;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes = {BlockSynchronizer.class})
public class BlockSynchronizerTest extends BaseCachingTest {
  
  @MockBean
  private BlockStorageService blockService;
  @MockBean
  private BlockMaker blockMaker;
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
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(true,153l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    Collections.shuffle(infoResult.getBlockInfos());
    synchronizer.synchronize(infoResult);
    
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
    assertNotNull("verify first requests headers are not null", batchRequests.get(0).getWantedHeaders());
    assertEquals("verify first requests headers size", 10, batchRequests.get(0).getWantedHeaders().size());
    assertEquals("verify first requests first header has correct hash", "hash123", batchRequests.get(0).getWantedHeaders().get(0).getHash());
    assertEquals("verify first requests first header has correct position", 123l, batchRequests.get(0).getWantedHeaders().get(0).getPosition().longValue());
    assertEquals("verify first requests fetch retry count", 5, batchRequests.get(0).getMaxFetchRetries());
    assertEquals("verify first requests ideal node count", 1, batchRequests.get(0).getIdealReceiveNodeCount());
    assertEquals("verify first requests correct start position", 123l, batchRequests.get(0).getFromPosition());
    assertEquals("verify first requests correct target topic", MainTopics.SYNC_BLOCKCHAIN, batchRequests.get(0).getTopic());
    assertEquals("verify second request start position", 133l, batchRequests.get(1).getFromPosition());
    assertEquals("verify third request start position", 143l, batchRequests.get(2).getFromPosition());
    assertEquals("verify fourth request start position", 153l, batchRequests.get(3).getFromPosition());
    assertEquals("verify after processing syncing status is on Done", SyncStatus.DONE.name(),
        syncBlockIdCache.getIfPresent(SyncStatus.SYNC_STATUS_CACHE_KEY));
    assertTrue("verify batch request contains selected Node ID's", batchRequests.get(0).getSelectedNodeIds().size() > 0);
    
    
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  
  
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withValidLastRespnseReturnsNoBlocks_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(false,153l,new ArrayList<>()));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(10 * 3)).saveInBlockchain(any());
    verify(inquirer, times(1 * 3)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withValidLastRespnseReturnsNullBlocks_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(false,153l, null));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(10 * 3)).saveInBlockchain(any());
    verify(inquirer, times(1 * 3)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withValidTheoretialltLastRespnseReturnsNullBlocks_shouldRetryAndSuccess() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(false,153l, null))
    .thenReturn(getFakeResponse(false,153l));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,153l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(10 * 4)).saveInBlockchain(any());
    verify(inquirer, times(1 * 5)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withFaultyResponseInTheMiddleTwiceAndInTheEnd_shouldRetryAndSuccess() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,233l,new ArrayList<>())).thenReturn(getFakeResponse(false,33l,new ArrayList<>()))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l))
    .thenReturn(getFakeResponse(true,3l, new ArrayList<>()))
    .thenReturn(getFakeResponse(false,153l));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,153l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(10 * 4)).saveInBlockchain(any());
    verify(inquirer, times(1 * 7)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withLastTotallyEmptyInsteadResponseFromInquirer_shouldWorkWell() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(new ArrayList<>());
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(10 * 3)).saveInBlockchain(any());
    verify(inquirer, times(1 * 3)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withOnlyOneEmptyResponse_shouldDoNothing() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(true,123l,new ArrayList<>()));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(0)).saveInBlockchain(any());
    verify(inquirer, times(0)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withOnlyOneEmptyResponseButBlockInfos_shouldRetryManyTimesAndGiveUp() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(true,123l,new ArrayList<>()));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(0)).saveInBlockchain(any());
    verify(inquirer, times(20)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withStorageFailingInTheMiddle_shouldRetryThatOne() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,123l)).thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l))
    .thenReturn(getFakeResponse(true,153l));
    when(blockService.saveInBlockchain(any())).thenThrow(BlockValidationException.class).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(true,153l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);

    verify(blockService, times(41)).saveInBlockchain(any());
    var batchCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer, times(1 * 5)).fetchNextBatch(batchCaptor.capture(), any());
    var batchRequests = batchCaptor.getAllValues();
    assertEquals("verify first requests batch size", 10, batchRequests.get(0).getBatchSize());
    assertEquals("verify first requests correct start position", 123l, batchRequests.get(0).getFromPosition());
    assertEquals("verify first requests correct target topic", MainTopics.SYNC_BLOCKCHAIN, batchRequests.get(0).getTopic());
    assertEquals("verify first retry request start position", 123l, batchRequests.get(1).getFromPosition());
    assertEquals("verify second request start position", 133l, batchRequests.get(2).getFromPosition());
    assertEquals("verify third request start position", 143l, batchRequests.get(3).getFromPosition());
    assertEquals("verify fourth request start position", 153l, batchRequests.get(4).getFromPosition());
    
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_withStorageFailingAgainAndAgain_shouldGiveUpWithoutException() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l));
    when(blockService.saveInBlockchain(any())).thenThrow(BlockValidationException.class);
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,123l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,133l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    infoResult.getBlockInfos().addAll(getFakeResponse(false,143l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    synchronizer.synchronize(infoResult);

    verify(blockService, times(20)).saveInBlockchain(any());
    verify(inquirer, times(20)).fetchNextBatch(any(), any());
    var cmdCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(cmdCaptor.capture());
    assertTrue("verify block generation stop and restart are called before/after sync", 
        ImmutableList.of(STOP, RESTART).containsAll(cmdCaptor.getAllValues()));
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_whenItsCurrentlyAlreadySyncing_shouldDoNothing() {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class))).thenReturn(getFakeResponse(false,123l))
    .thenReturn(getFakeResponse(false,133l)).thenReturn(getFakeResponse(false,143l)).thenReturn(getFakeResponse(true,153l));
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    syncBlockIdCache.put(SyncStatus.SYNC_STATUS_CACHE_KEY, SyncStatus.IN_PROGRESS.name());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    synchronizer.synchronize(infoResult);
    
    verify(blockService, times(0)).saveInBlockchain(any());
    verify(inquirer, times(0)).fetchNextBatch(any(), any(Class.class));
    verify(blockMaker,never()).generation(any());
  }
  
  @Test
  @SuppressWarnings("unchecked")
  public void testSynchronize_whenRequestingSyncTwiceAtTheSameTime_shouldOnlyCallOnce() throws Exception {
    when(inquirer.fetchNextBatch(any(SyncBatchRequest.class), any(Class.class)))
    .thenAnswer(new Answer<List<SyncResponse<Block>>>() {
      @Override
      public List<SyncResponse<Block>> answer(InvocationOnMock invocation) throws Throwable {
        Thread.sleep(200);
        return getFakeResponse(true,153l);
      }});
    when(blockService.saveInBlockchain(any())).thenReturn(new StoredBlock());
    
    BlockInfoResult infoResult = new BlockInfoResult(generateNodeIds("id",20),new ArrayList<>());
    infoResult.getBlockInfos().addAll(getFakeResponse(false,153l).get(0).getEntities().stream().map(this::mapToInfo).collect(Collectors.toList()));
    Collections.shuffle(infoResult.getBlockInfos());
    
    ExecutorService service = Executors.newFixedThreadPool(10);
    var hashables = IntStream.range(0, 2).mapToObj(count -> new SyncRunnable(infoResult, synchronizer)).collect(Collectors.toList());
    service.invokeAll(hashables); // invoke simultaneously
    Thread.sleep(200);
    
    verify(blockService, times(10)).saveInBlockchain(any());
    verify(inquirer, times(1)).fetchNextBatch(any(), any(Class.class));
    verify(blockMaker, times(2)).generation(any());
  }
  
  private static class SyncRunnable implements Callable<String> {
    private BlockInfoResult infoResult;
    private BlockSynchronizer synchronizer;
    public SyncRunnable(BlockInfoResult infoResult, BlockSynchronizer synchronizer) {
      this.infoResult = infoResult;
      this.synchronizer = synchronizer;
    }
    @Override
    public String call() throws Exception {
      synchronizer.synchronize(infoResult);
      return "";
    }
  }
  
  @Test
  public void testSyncStatus_withSetStatus_shouldReturnCorrect() {
    syncBlockIdCache.put(SyncStatus.SYNC_STATUS_CACHE_KEY, SyncStatus.IN_PROGRESS.name());
    
    String status = synchronizer.syncStatus();
    
    assertNotNull("verify status is not null", status);
    assertEquals("verify status is correct", SyncStatus.IN_PROGRESS.name(), status);
  }
  
  @Test
  public void testSyncStatus_withNotSetStatus_shouldReturnDefault() {    
    syncBlockIdCache.cleanUp();
    String status = synchronizer.syncStatus();
    assertNotNull("verify status is not null", status);
    assertEquals("verify status is correct", SyncStatus.DONE.name(), status);
  }
 
  
  List<SyncResponse<Block>> getFakeResponse(boolean isLast, long startPos) {
    var blocks = new ArrayList<Block>();
    var block1 = new Block();
    block1.setPosition(123l);
    block1.setHash("hash123");
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
  
  List<SyncResponse<Block>> getFakeResponse(boolean isLast, long startPos, List<Block> blocks) {
    var response = new SyncResponse<Block>();
    response.setEntities(blocks);
    response.setLastPositionReached(isLast);
    response.setStartingPosition(startPos);
    return ListUtils.emptyIfNull(ImmutableList.of(response));
  }
  
  private BlockInfo mapToInfo(Block block) {
    var info = new BlockInfo();
    info.setBlockHash(block.getHash());
    info.setPosition(block.getPosition());
    return info;
  }
}
