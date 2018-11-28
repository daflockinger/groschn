package com.flockinger.groschn.blockchain.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.messaging.sync.FullSyncKeeper;
import com.flockinger.groschn.blockchain.messaging.sync.GlobalBlockchainStatistics;
import com.flockinger.groschn.blockchain.messaging.sync.SmartBlockSynchronizer;
import com.flockinger.groschn.blockchain.messaging.sync.StartupSynchronizator;
import com.flockinger.groschn.commons.exception.BlockchainException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {StartupConfig.class, StartupSynchronizator.class})
@TestExecutionListeners(listeners= {DependencyInjectionTestExecutionListener.class, 
    MockitoTestExecutionListener.class, ResetMocksTestExecutionListener.class})
public class StartupSynchronizatorTest {

  @MockBean(reset=MockReset.BEFORE)
  private FullSyncKeeper transactionFullSynchronizer;
  @MockBean(reset=MockReset.BEFORE)
  private SmartBlockSynchronizer blockSynchronizer;
  @MockBean(reset=MockReset.BEFORE)
  private GlobalBlockchainStatistics statistics;
  
  @Autowired
  private StartupSynchronizator startuper;
  
  @Test
  public void testAfterPropertiesSet_withEverithingFine_shouldCallBlockAndTransactionSync() throws Exception {
    when(statistics.lastBlockPosition()).thenReturn(Optional.of(99L));
    startuper.afterPropertiesSet();
    
    verify(blockSynchronizer, times(1)).sync(eq(99L));
    verify(transactionFullSynchronizer, times(1)).fullSynchronization();
  }
  
  @Test
  public void testAfterPropertiesSet_withBlockSynchFailing_shouldDoNothingLogError() throws Exception {
    when(statistics.lastBlockPosition()).thenReturn(Optional.of(99L));
    doThrow(BlockchainException.class).when(blockSynchronizer).sync(any());
    
    startuper.afterPropertiesSet();
    
    verify(blockSynchronizer, times(1)).sync(any());
    verify(transactionFullSynchronizer, never()).fullSynchronization();
  }

  @Test
  public void testAfterPropertiesSet_withDeterminingLastBlockPositionReturningEmpty_shouldDoNothingLogWarning() throws Exception {
    when(statistics.lastBlockPosition()).thenReturn(Optional.empty());

    startuper.afterPropertiesSet();

    verify(blockSynchronizer,never()).sync(any());
    verify(transactionFullSynchronizer, never()).fullSynchronization();
  }
}
