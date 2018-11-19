package com.flockinger.groschn.blockchain.messaging.sync;

import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStrategyType;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.SmartBlockSynchronizerImpl;
import com.flockinger.groschn.blockchain.messaging.sync.strategy.ConfidentSyncStrategy;
import com.flockinger.groschn.blockchain.messaging.sync.strategy.ScanningSyncStrategy;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmartBlockSynchronizerImpl.class})
public class SmartBlockSynchronizerTest {

  @MockBean
  private ConfidentSyncStrategy confidentMock;
  @MockBean
  private ScanningSyncStrategy scanMock;
  @MockBean
  private BlockSynchronizer blockSynchronizer;
  
  @Autowired
  private SmartBlockSynchronizerImpl synchronizer;  
  
  @Before
  public void setup() {
    when(confidentMock.isApplicable(eq(SyncStrategyType.CONFIDENT))).thenReturn(true);
    when(scanMock.isApplicable(eq(SyncStrategyType.SCAN))).thenReturn(true);
    when(scanMock.isApplicable(eq(SyncStrategyType.FALLBACK))).thenReturn(true);
  }
  
  @Test
  public void testSync_withConfidentStratSuccess_shouldApplyAndSync() {
    when(confidentMock.apply(any())).thenReturn(Optional.of(new BlockInfoResult(null, null)));
    
    synchronizer.sync(SyncSettings.confident(23L, 32L));
    
    verify(confidentMock).apply(any());
    verify(scanMock, never()).apply(any());
    verify(blockSynchronizer).synchronize(notNull());
  }
  
  @Test
  public void testSync_withScanStratFails_shouldApplyFallbackAndSync() {
    when(confidentMock.apply(any())).thenReturn(Optional.empty());
    when(scanMock.apply(any())).thenReturn(Optional.of(new BlockInfoResult(null, null)));
    
    synchronizer.sync(SyncSettings.confident(23L, 32L));
    
    verify(scanMock).apply(any());
    verify(confidentMock).apply(any());
    verify(blockSynchronizer).synchronize(notNull());
  }
  
  @Test
  public void testSync_withScanStratSuccess_shouldApplyAndSync() {
    when(scanMock.apply(any())).thenReturn(Optional.of(new BlockInfoResult(null, null)));
    
    synchronizer.sync(SyncSettings.scan(23L));
    
    verify(scanMock).apply(any());
    verify(confidentMock, never()).apply(any());
    verify(blockSynchronizer).synchronize(notNull());
  }
}
