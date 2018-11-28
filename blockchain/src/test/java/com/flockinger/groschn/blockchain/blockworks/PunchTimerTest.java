package com.flockinger.groschn.blockchain.blockworks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.blockworks.dto.BlockGenerationStatus;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class,
    classes = {PunchTimer.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    MockitoTestExecutionListener.class, ResetMocksTestExecutionListener.class})
public class PunchTimerTest {

  @MockBean(reset = MockReset.BEFORE)
  private ConsensusFactory consensusFactory;
  @MockBean(reset = MockReset.BEFORE)
  private BlockMaker blockMaker;
  @MockBean(reset = MockReset.BEFORE)
  private BlockSynchronizer blockSynchronizer;

  @Autowired
  private PunchTimer timer;

  @Test
  public void testCheckMiningProcess_withNoRunningProcesses_shouldStartOne() {
    when(blockSynchronizer.syncStatus()).thenReturn(SyncStatus.DONE.name());
    when(blockMaker.status()).thenReturn(BlockGenerationStatus.COMPLETE);

    timer.checkMiningProcess();

    verify(blockMaker, times(1)).generation(any());
  }

  @Test
  public void testCheckMiningProcess_withRunningProcess_shouldDoNothing() {
    when(blockSynchronizer.syncStatus()).thenReturn(SyncStatus.DONE.name());
    when(blockMaker.status()).thenReturn(BlockGenerationStatus.RUNNING);

    timer.checkMiningProcess();

    verify(blockMaker, never()).generation(any());
  }

  @Test
  public void testCheckMiningProcess_withStoppedProcess_shouldDoNothing() {
    when(blockSynchronizer.syncStatus()).thenReturn(SyncStatus.DONE.name());
    when(blockMaker.status()).thenReturn(BlockGenerationStatus.STOPPED);

    timer.checkMiningProcess();

    verify(blockMaker, never()).generation(any());
  }

  @Test
  public void testCheckMiningProcess_withRunningSyncInBackground_shouldDoNothing() {
    when(blockSynchronizer.syncStatus()).thenReturn(SyncStatus.IN_PROGRESS.name());

    timer.checkMiningProcess();

    verify(blockMaker, times(0)).generation(any());
  }
}
