package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import static com.flockinger.groschn.blockchain.TestDataFactory.fakeBlockInfos;
import static com.flockinger.groschn.blockchain.TestDataFactory.fakeBlocks;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStrategyType;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ScanningSyncStrategy.class, ScanResultMatcher.class})
public class ScanningSyncStrategyTest {
  
  @MockBean
  private BlockInfoResultProvider infoResultProvider;
  @MockBean
  private BlockStorageService blockService;
  
  @Autowired
  private ScanningSyncStrategy scanner;
  
  //TODO maybe think about more tests?
  
  @Test
  public void testApply_withFoundMatchOnFirstBatch_shouldReturnCorrect() {
    var nodes = TestDataFactory.generateNodeIds("id", 23);
    nodes.set(1, "id0");
    var infos =  fakeBlockInfos(10,12,"hash");
    infos.get(9).setBlockHash("wrong");
    infos.get(8).setBlockHash("wrong");
    infos.get(7).setBlockHash("wrong");
    infos.get(6).setBlockHash("wrong");
    when(infoResultProvider.fetchBlockInfos(anyLong(), anyInt())).thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, infos)));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(fakeBlocks(10, 12, "hash"));
    
    
    var result = scanner.apply(SyncSettings.scan(12L));
    
    assertTrue("verify BlockInfoResult is returned", result.isPresent());
    var returnedNodes = result.get().getNodeIds();
    var wantedNodes = TestDataFactory.generateNodeIds("id", 23);
    wantedNodes.remove(1);
    assertTrue("verify that all nodeIds are present", returnedNodes.containsAll(wantedNodes));
    var returnedInfos = result.get().getBlockInfos();
    assertEquals("verify correct returned to sync info size", 5, returnedInfos.size());
    assertTrue("verify that correct blockInfos are returned", returnedInfos.containsAll(infos.subList(5, 10)));
    
    verify(infoResultProvider).fetchBlockInfos(eq(12L), eq(10));
    verify(blockService).findBlocks(eq(12L), eq(10L));
  }
  
  
  
  @Test
  public void testApply_withFoundMatchOnThirdBatch_shouldReturnAllInfoBatches() {
    var nodes = TestDataFactory.generateNodeIds("id", 23);
    var infos =  fakeBlockInfos(10,32,"hash");
    infos.get(9).setBlockHash("wrong");
    infos.get(8).setBlockHash("wrong");
    when(infoResultProvider.fetchBlockInfos(eq(52L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,52,"hish"))));
    when(infoResultProvider.fetchBlockInfos(eq(42L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,42,"hush"))));
    when(infoResultProvider.fetchBlockInfos(eq(32L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, infos)));
    
    when(blockService.findBlocks(eq(52L), eq(10L)))
        .thenReturn(fakeBlocks(10, 52, "hash"));
    when(blockService.findBlocks(eq(42L), eq(10L)))
        .thenReturn(fakeBlocks(10, 42, "hash"));
    when(blockService.findBlocks(eq(32L), eq(10L)))
        .thenReturn(fakeBlocks(10, 32, "hash"));
    
    var result = scanner.apply(SyncSettings.scan(52L));
    
    assertTrue("verify BlockInfoResult is returned", result.isPresent());
    var returnedNodes = result.get().getNodeIds();
    assertTrue("verify that all nodeIds are present", returnedNodes.containsAll(TestDataFactory.generateNodeIds("id", 23)));
    var returnedInfos = result.get().getBlockInfos();
    assertEquals("verify correct returned to sync info size", 23, returnedInfos.size());
    assertTrue("verify that correct blockInfos are returned", 
           returnedInfos.containsAll(infos.subList(7, 10)) 
        && returnedInfos.containsAll(fakeBlockInfos(10,52,"hish")) 
        && returnedInfos.containsAll(fakeBlockInfos(10,42,"hush")) );
    
    verify(infoResultProvider, times(3)).fetchBlockInfos(anyLong(), anyInt());
    verify(blockService, times(3)).findBlocks(anyLong(), anyLong());
  }
  
  @Test
  public void testApply_withNoStoredBlocksReturnedAtFirstFoundMatchOnThirdBatch_shouldReturnAllInfoBatches() {
    var nodes = TestDataFactory.generateNodeIds("id", 23);
    var infos =  fakeBlockInfos(10,32,"hash");
    infos.get(9).setBlockHash("wrong");
    infos.get(8).setBlockHash("wrong");
    when(infoResultProvider.fetchBlockInfos(eq(52L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,52,"hish"))));
    when(infoResultProvider.fetchBlockInfos(eq(42L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,42,"hush"))));
    when(infoResultProvider.fetchBlockInfos(eq(32L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, infos)));
    
    when(blockService.findBlocks(eq(52L), eq(10L)))
        .thenReturn(new ArrayList<>());
    when(blockService.findBlocks(eq(42L), eq(10L)))
        .thenReturn(fakeBlocks(10, 42, "hash"));
    when(blockService.findBlocks(eq(32L), eq(10L)))
        .thenReturn(fakeBlocks(10, 32, "hash"));
    
    var result = scanner.apply(SyncSettings.scan(52L));
    
    assertTrue("verify BlockInfoResult is returned", result.isPresent());
    var returnedNodes = result.get().getNodeIds();
    assertTrue("verify that all nodeIds are present", returnedNodes.containsAll(TestDataFactory.generateNodeIds("id", 23)));
    var returnedInfos = result.get().getBlockInfos();
    assertEquals("verify correct returned to sync info size", 23, returnedInfos.size());
    assertTrue("verify that correct blockInfos are returned", 
           returnedInfos.containsAll(infos.subList(7, 10)) 
        && returnedInfos.containsAll(fakeBlockInfos(10,52,"hish")) 
        && returnedInfos.containsAll(fakeBlockInfos(10,42,"hush")) );
    
    verify(infoResultProvider, times(3)).fetchBlockInfos(anyLong(), anyInt());
    verify(blockService, times(3)).findBlocks(anyLong(), anyLong());
  }
  
  @Test
  public void testApply_withScannedAllTheWayToFirstBlock_shouldReturnAllInfos() {
    var nodes = TestDataFactory.generateNodeIds("id", 23);
    var infos =  fakeBlockInfos(5,1,"hosh");
    infos.get(0).setBlockHash("hash");
    when(infoResultProvider.fetchBlockInfos(eq(15L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,15,"hish"))));
    when(infoResultProvider.fetchBlockInfos(eq(5L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,5,"hush"))));
    when(infoResultProvider.fetchBlockInfos(eq(1L), eq(5)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, infos)));
    
    when(blockService.findBlocks(eq(15L), eq(10L)))
        .thenReturn(fakeBlocks(10, 15, "hash"));
    when(blockService.findBlocks(eq(5L), eq(10L)))
        .thenReturn(fakeBlocks(10, 5, "hash"));
    when(blockService.findBlocks(eq(1L), eq(5L)))
        .thenReturn(fakeBlocks(10, 1, "hash"));
    
    var result = scanner.apply(SyncSettings.scan(15L));
    
    assertTrue("verify BlockInfoResult is returned", result.isPresent());
    var returnedNodes = result.get().getNodeIds();
    assertTrue("verify that all nodeIds are present", returnedNodes.containsAll(TestDataFactory.generateNodeIds("id", 23)));
    var returnedInfos = result.get().getBlockInfos();
    assertEquals("verify correct returned to sync info size", 25, returnedInfos.size());
    assertTrue("verify that correct blockInfos are returned", 
           returnedInfos.containsAll(infos) 
        && returnedInfos.containsAll(fakeBlockInfos(10,15,"hish")) 
        && returnedInfos.containsAll(fakeBlockInfos(10,5,"hush")) );
    
    verify(infoResultProvider, times(3)).fetchBlockInfos(anyLong(), anyInt());
    verify(blockService, times(3)).findBlocks(anyLong(), anyLong());
  }
  
  @Test
  public void testApply_withEmptyBlockchainNoDBEntries_shouldReturnAllInfos() {
    var nodes = TestDataFactory.generateNodeIds("id", 23);
    var infos =  fakeBlockInfos(5,1,"hosh");
    infos.get(0).setBlockHash("hash");
    when(infoResultProvider.fetchBlockInfos(eq(15L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,15,"hish"))));
    when(infoResultProvider.fetchBlockInfos(eq(5L), eq(10)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, fakeBlockInfos(10,5,"hush"))));
    when(infoResultProvider.fetchBlockInfos(eq(1L), eq(5)))
        .thenReturn(Optional.ofNullable(new BlockInfoResult(nodes, infos)));
    
    when(blockService.findBlocks(eq(15L), eq(10L)))
        .thenReturn(new ArrayList<>());
    
    var result = scanner.apply(SyncSettings.scan(15L));
    
    assertTrue("verify BlockInfoResult is returned", result.isPresent());
    var returnedNodes = result.get().getNodeIds();
    assertTrue("verify that all nodeIds are present", returnedNodes.containsAll(TestDataFactory.generateNodeIds("id", 23)));
    var returnedInfos = result.get().getBlockInfos();
    assertEquals("verify correct returned to sync info size", 25, returnedInfos.size());
    assertTrue("verify that correct blockInfos are returned", 
           returnedInfos.containsAll(infos) 
        && returnedInfos.containsAll(fakeBlockInfos(10,15,"hish")) 
        && returnedInfos.containsAll(fakeBlockInfos(10,5,"hush")) );
    
    verify(infoResultProvider, times(3)).fetchBlockInfos(anyLong(), anyInt());
    verify(blockService, times(3)).findBlocks(anyLong(), anyLong());
  }
  
  @Test
  public void testIsApplicable_shouldReturnCorrect() {
    assertTrue("should be applicable as fallback", scanner.isApplicable(SyncStrategyType.FALLBACK));
    assertTrue("should be applicable as scanner", scanner.isApplicable(SyncStrategyType.SCAN));
    assertFalse("should NOT be applicable as confident", scanner.isApplicable(SyncStrategyType.CONFIDENT));
  }
}
