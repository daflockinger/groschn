package com.flockinger.groschn.blockchain.consensus;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.consensus.model.ProofOfMajorityConsent;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.messaging.members.ElectionStatistics;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ConsensusFactory.class})
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
  private ConsensusFactory factory;
  
  @Test
  public void testReachConsensus_withNotEnoughBlocksForPoM_shouldTryPoWConsensus() {
    
    when(statsMock.currentActiveVoterCount()).thenReturn(101l);
    when(daoMock.count()).thenReturn(ProofOfMajorityConsent.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM - 1);
    
    factory.reachConsensus(new ArrayList<>());
    
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
    
    factory.reachConsensus(new ArrayList<>());
    
    verify(powMock, times(0)).reachConsensus(any());
    verify(proOfMajorityMock, times(1)).reachConsensus(any());
  }
}
