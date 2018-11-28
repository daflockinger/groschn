package com.flockinger.groschn.blockchain.messaging.sync;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.exception.BlockSynchronizationException;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.SmartBlockSynchronizerImpl;
import com.flockinger.groschn.blockchain.messaging.sync.strategy.ScanningSyncStrategy;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmartBlockSynchronizerImpl.class})
public class SmartBlockSynchronizerTest {

  @MockBean
  private ScanningSyncStrategy scanMock;
  @MockBean
  private BlockSynchronizer blockSynchronizer;
  @MockBean
  private BlockMaker blockMaker;
  
  @Autowired
  private SmartBlockSynchronizerImpl synchronizer;  
  
  
  @Test
  public void testSync_withScanStratSuccess_shouldApplyAndSync() {
    when(scanMock.apply(any())).thenReturn(Optional.of(new BlockInfoResult(null, null)));
    
    synchronizer.sync(23L);
    
    verify(scanMock).apply(any());
    verify(blockSynchronizer).synchronize(notNull());
  }
  
  
  
  @Test
  public void testSync_withScanStratThrowingException_shouldDoNothing() {
    when(scanMock.apply(any())).thenThrow(BlockSynchronizationException.class);
    
    synchronizer.sync(23L);
    
    verify(scanMock).apply(any());
    verify(blockSynchronizer, never()).synchronize(notNull());
  }
  
  @Test
  public void testSync_withScanStratReturningEmpty_shouldDoNothing() {
    when(scanMock.apply(any())).thenReturn(Optional.empty());
    
    synchronizer.sync(23L);
    
    verify(scanMock, times(1)).apply(any());
    verify(blockSynchronizer, never()).synchronize(notNull());
  }
  
  
  @Test
  public void testSync_whenRequestingSyncTwiceAtTheSameTime_shouldOnlyCallOnce() throws Exception {
    when(scanMock.apply(any()))
    .thenAnswer(new Answer<Optional<BlockInfoResult>>() {
      @Override
      public Optional<BlockInfoResult> answer(InvocationOnMock invocation) throws Throwable {
        Thread.sleep(200);
        return Optional.of(new BlockInfoResult(null, null));
      }});
    
    ExecutorService service = Executors.newFixedThreadPool(10);
    var hashables = IntStream.range(0, 2).mapToObj(count -> new SyncRunnable(synchronizer)).collect(Collectors.toList());
    service.invokeAll(hashables); // invoke simultaneously
    Thread.sleep(200);
    
    
    verify(scanMock).apply(any());
    verify(blockSynchronizer).synchronize(notNull());
  }
  
  private static class SyncRunnable implements Callable<String> {
    private SmartBlockSynchronizerImpl synchronizer;
    public SyncRunnable( SmartBlockSynchronizerImpl synchronizer) {
      this.synchronizer = synchronizer;
    }
    @Override
    public String call() throws Exception {
      synchronizer.sync(0L);
      return "";
    }
  }
}
