package com.flockinger.groschn.blockchain.blockworks;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.Optional;
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
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers=ConfigFileApplicationContextInitializer.class, classes = {PunchTimer.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class, 
  ResetMocksTestExecutionListener.class})
public class PunchTimerTest {

  @MockBean(reset=MockReset.BEFORE)
  private ConsensusFactory consensusFactory;
  @MockBean(reset=MockReset.BEFORE)
  private BlockMaker blockMaker;
  
  @Autowired
  private PunchTimer timer;
  
  
  @Test
  public void testCheckMiningProcess_withRunningProcessNoTimeout_shouldDoNothing() {
    when(consensusFactory.isProcessing()).thenReturn(true);
    Date stillOkDate = new Date(new Date().getTime() - 15000);
    when(consensusFactory.lastProcessStartDate()).thenReturn(Optional.of(stillOkDate));
    
    timer.checkMiningProcess();
    
    verify(blockMaker, times(0)).produceBlock();
    
  }
  
  @Test
  public void testCheckMiningProcess_withNoRunningProcesses_shouldStartOne() {
    when(consensusFactory.isProcessing()).thenReturn(false);
   
    timer.checkMiningProcess();
    
    verify(blockMaker, times(1)).produceBlock();
  }
  
  @Test
  public void testCheckMiningProcess_withRunningProcessVeryOld_shouldStopAndStartNewOne() {
    when(consensusFactory.isProcessing()).thenReturn(true);
    Date stillOkDate = new Date(new Date().getTime() - 21001);
    when(consensusFactory.lastProcessStartDate()).thenReturn(Optional.of(stillOkDate));
    
    timer.checkMiningProcess();
    
    verify(consensusFactory, times(1)).stopFindingConsensus();
    verify(blockMaker, times(1)).produceBlock();
  }
}
