package com.flockinger.groschn.blockchain.messaging.sync.strategy;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Optional;
import static com.flockinger.groschn.blockchain.TestDataFactory.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStrategyType;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ConfidentSyncStrategy.class})
public class ConfidentSyncStrategyTest {

  @MockBean
  private BlockInfoResultProvider infoResultProvider;
  @MockBean
  private BlockStorageService blockService;

  @Autowired
  private ConfidentSyncStrategy strategy;
  
  
  @Test
  public void testApply_withFoundOverlapCorrect_shouldReturnCorrect() {
    when(infoResultProvider.fetchBlockInfos(eq(78L), eq(5)))
        .thenReturn(Optional.of(new BlockInfoResult(generateNodeIds("id", 20), fakeBlockInfos(5, 78, "hash"))));
    when(blockService.findBlocks(eq(78L), eq(1L))).thenReturn(fakeBlocks(1, 78, "hash"));
    
    var result = strategy.apply(SyncSettings.confident(78L, 83L));
    
    assertTrue("verify that info result is present", result.isPresent());
  }
  
  @Test
  public void testApply_withFoundOverlapFaulty_shouldReturnEmpty() {
    when(infoResultProvider.fetchBlockInfos(eq(78L), eq(5)))
        .thenReturn(Optional.of(new BlockInfoResult(generateNodeIds("id", 20), fakeBlockInfos(5, 78, "wrong"))));
    when(blockService.findBlocks(eq(78L), eq(1L))).thenReturn(fakeBlocks(1, 78, "hash"));
    
    var result = strategy.apply(SyncSettings.confident(78L, 83L));
    
    assertFalse("verify with faulty overlap it returned empty", result.isPresent());
  }
  
  @Test
  public void testApply_withNoOverlapFound_shouldReturnEmpty() {
    when(infoResultProvider.fetchBlockInfos(eq(90L), eq(2)))
        .thenReturn(Optional.of(new BlockInfoResult(generateNodeIds("id", 20), fakeBlockInfos(2, 90, "hush"))));
    when(blockService.findBlocks(eq(90L), eq(1L))).thenReturn(new ArrayList<>());
    
    var result = strategy.apply(SyncSettings.confident(90L, 92L));
    
    assertFalse("verify with no overlap it returned empty", result.isPresent());
  }
  
  
  @Test
  public void testIsApplicable_shouldReturnCorrect() {
    assertTrue("should NOT be applicable as confident",
        strategy.isApplicable(SyncStrategyType.CONFIDENT));
    assertFalse("should NOT be applicable as fallback",
        strategy.isApplicable(SyncStrategyType.FALLBACK));
    assertFalse("should NOT be applicable as scanner",
        strategy.isApplicable(SyncStrategyType.SCAN));
  }
}
