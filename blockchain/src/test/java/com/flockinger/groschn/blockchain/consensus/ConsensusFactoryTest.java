package com.flockinger.groschn.blockchain.consensus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.config.GeneralConfig;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.ProofOfMajorityConsent;
import com.flockinger.groschn.blockchain.exception.ReachingConsentFailedException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockProcessRepository;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.BlockProcess;
import com.flockinger.groschn.blockchain.repository.model.ProcessStatus;
import com.flockinger.groschn.messaging.members.ElectionStatistics;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DataMongoTest
@Import({GeneralConfig.class, CryptoConfig.class})
@ContextConfiguration(classes = {ConsensusFactory.class, BlockProcessRepository.class})
public class ConsensusFactoryTest {

  @MockBean(name="POW")
  private ConsensusAlgorithm powMock;
  @MockBean(name="ProofOfMajority")
  private ConsensusAlgorithm proOfMajorityMock;
  @MockBean
  private ElectionStatistics statsMock;
  @MockBean
  private BlockchainRepository daoMock;
  @Autowired
  private BlockProcessRepository processDao;
  
  @Autowired
  private ConsensusFactory factory;
  
  @Before
  public void setup() {
    processDao.deleteAll();
  }
  
  @Test
  public void testReachConsensus_withNotEnoughBlocksForPoM_shouldTryPoWConsensus() {
    
    when(statsMock.currentActiveVoterCount()).thenReturn(101l);
    when(daoMock.count()).thenReturn(ProofOfMajorityConsent.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM - 1);
    
    factory.reachConsensus(new ArrayList<>());
    
    Optional<BlockProcess> process = processDao.findFirstByOrderByStartedAtDesc();
    assertTrue("verify that block process statistics was stored", process.isPresent());
    assertNotNull("verify that start date was set", process.get().getStartedAt());
    assertNotNull("verify that finished date was set", process.get().getFinishedAt());
    assertEquals("verify that correct consensus type", ConsensusType.PROOF_OF_WORK, process.get().getConsensus());
    assertEquals("verify that correct process status", ProcessStatus.DONE, process.get().getStatus());
    
    verify(powMock, times(1)).reachConsensus(any());
    verify(proOfMajorityMock, times(0)).reachConsensus(any());
  }
  
  @Test
  public void testReachConsensus_withNotEnoughVoters_shouldTryPoWConsensus() {
    
    when(statsMock.currentActiveVoterCount()).thenReturn(99l);
    when(daoMock.count()).thenReturn(ProofOfMajorityConsent.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM + 1);
    
    factory.reachConsensus(new ArrayList<>());
    
    verify(powMock, times(1)).reachConsensus(any());
    verify(proOfMajorityMock, times(0)).reachConsensus(any());
  }
  
  @Test
  public void testReachConsensus_withConditionsMetForProofOfMajority_shouldTryPoMConsensus() {
    when(statsMock.currentActiveVoterCount()).thenReturn(101l);
    when(daoMock.count()).thenReturn(ProofOfMajorityConsent.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM + 1);
    when(proOfMajorityMock.reachConsensus(any())).thenReturn(new Block());
    
    factory.reachConsensus(new ArrayList<>());
    
    Optional<BlockProcess> process = processDao.findFirstByOrderByStartedAtDesc();
    assertTrue("verify that block process statistics was stored", process.isPresent());
    assertNotNull("verify that start date was set", process.get().getStartedAt());
    assertNotNull("verify that finished date was set", process.get().getFinishedAt());
    assertEquals("verify that correct consensus type", ConsensusType.PROOF_OF_MAJORITY, process.get().getConsensus());
    assertEquals("verify that correct process status", ProcessStatus.DONE, process.get().getStatus());
    
    verify(powMock, times(0)).reachConsensus(any());
    verify(proOfMajorityMock, times(1)).reachConsensus(any());
  }
  
  @Test
  public void testReachConsensus_withConditionsMetForProofOfMajorityButItFails_shouldTryPoMConsensusAndFallbackToPoW() {
    when(statsMock.currentActiveVoterCount()).thenReturn(101l);
    when(daoMock.count()).thenReturn(ProofOfMajorityConsent.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM + 1);
    when(proOfMajorityMock.reachConsensus(any())).thenThrow(ReachingConsentFailedException.class);
    
    factory.reachConsensus(new ArrayList<>());
    
    Optional<BlockProcess> process = processDao.findFirstByOrderByStartedAtDesc();
    assertTrue("verify that block process statistics was stored", process.isPresent());
    assertNotNull("verify that start date was set", process.get().getStartedAt());
    assertNotNull("verify that finished date was set", process.get().getFinishedAt());
    assertEquals("verify that correct consensus type", ConsensusType.PROOF_OF_WORK, process.get().getConsensus());
    assertEquals("verify that correct process status", ProcessStatus.DONE, process.get().getStatus());
    
    verify(powMock, times(1)).reachConsensus(any());
    verify(proOfMajorityMock, times(1)).reachConsensus(any());
  }
  
  @Test
  public void testStopFindingConsensus_shouldStopBoth() {
    factory.stopFindingConsensus();
        
    verify(powMock,times(1)).stopFindingConsensus();
    verify(proOfMajorityMock,times(1)).stopFindingConsensus();
  }
  
  @Test
  public void testIsProcessing_ifOneConsensusIsRunning_shouldBeTrue() {
    when(powMock.isProcessing()).thenReturn(false);
    when(proOfMajorityMock.isProcessing()).thenReturn(true);
    
    assertEquals("verify that processing returns true if one of both consensus algos returns true", 
        true, factory.isProcessing());
  }
  
  @Test
  public void testLastProcessDate_whenStuffWasProcessedBefore_shouldReturnCorrect() {
    when(statsMock.currentActiveVoterCount()).thenReturn(99l);
    when(daoMock.count()).thenReturn(ProofOfMajorityConsent.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM + 1);
    
    factory.reachConsensus(new ArrayList<>());
    
    BlockProcess weirdProcess = new BlockProcess();
    weirdProcess.setStartedAt(new Date(1000l));
    processDao.save(weirdProcess);
    BlockProcess weirdProcess2 = new BlockProcess();
    weirdProcess2.setStartedAt(null);
    processDao.save(weirdProcess2);
    
    
    Optional<Date> lastProcessDate = factory.lastProcessStartDate();
    assertTrue("verify that last process date exists", lastProcessDate.isPresent());
    assertNotNull("verify that process date is really latest", lastProcessDate.get().getTime() > 1000l);
  }
}
