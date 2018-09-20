package com.flockinger.groschn.blockchain.messaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.messaging.sync.FullSyncKeeper;
import com.flockinger.groschn.blockchain.messaging.sync.StartupSynchronizator;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {StartupSynchronizator.class})
@TestExecutionListeners(listeners= {DependencyInjectionTestExecutionListener.class, 
    MockitoTestExecutionListener.class, ResetMocksTestExecutionListener.class})
public class StartupSynchronizatorTest {

  @MockBean(reset=MockReset.BEFORE)
  private FullSyncKeeper transactionFullSynchronizer;
  @MockBean(reset=MockReset.BEFORE)
  private SyncDeterminator blockSynchronizer;
  
  @Autowired
  private StartupSynchronizator startuper;
  
  @Test
  public void testAfterPropertiesSet_withEverithingFine_shouldCallBlockAndTransactionSync() throws Exception {
    
    startuper.afterPropertiesSet();
    
    verify(blockSynchronizer, times(1)).determineAndSync();
    verify(transactionFullSynchronizer, times(1)).fullSynchronization();
  }
  
  @Test
  public void testAfterPropertiesSet_withBlockSynchFailing_shouldDoNothingLogError() throws Exception {
    doThrow(BlockchainException.class).when(blockSynchronizer).determineAndSync();
    
    startuper.afterPropertiesSet();
    
    verify(blockSynchronizer, times(1)).determineAndSync();
    verify(transactionFullSynchronizer, times(0)).fullSynchronization();
  }
}
