package com.flockinger.groschn.blockchain.consensus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Optional;
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
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfMajorityAlgorithm;
import com.flockinger.groschn.blockchain.exception.ReachingConsentFailedException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockProcessRepository;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.messaging.members.NetworkStatistics;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DataMongoTest
@Import({GeneralConfig.class, CryptoConfig.class})
@ContextConfiguration(classes = {ConsensusFactory.class, BlockProcessRepository.class})
public class ConsensusFactoryTest {

  @MockBean(name = "POW")
  private ConsensusAlgorithm powMock;
  @MockBean(name = "ProofOfMajority")
  private ConsensusAlgorithm proOfMajorityMock;
  @MockBean
  private NetworkStatistics statsMock;
  @MockBean
  private BlockchainRepository daoMock;

  @Autowired
  private ConsensusFactory factory;


  @Test
  public void testReachConsensus_withNotEnoughBlocksForPoM_shouldTryPoWConsensus() {

    when(statsMock.activeNodeCount()).thenReturn(101l);
    when(daoMock.count())
        .thenReturn(ProofOfMajorityAlgorithm.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM - 1);

    factory.reachConsensus(new ArrayList<>());

    verify(powMock, times(1)).reachConsensus(any());
    verify(proOfMajorityMock, times(0)).reachConsensus(any());
  }

  @Test
  public void testReachConsensus_withNotEnoughVoters_shouldTryPoWConsensus() {

    when(statsMock.activeNodeCount()).thenReturn(99l);
    when(daoMock.count())
        .thenReturn(ProofOfMajorityAlgorithm.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM + 1);

    factory.reachConsensus(new ArrayList<>());

    verify(powMock, times(1)).reachConsensus(any());
    verify(proOfMajorityMock, times(0)).reachConsensus(any());
  }

  @Test
  public void testReachConsensus_withConditionsMetForProofOfMajority_shouldTryPoMConsensus() {
    when(statsMock.activeNodeCount()).thenReturn(101l);
    when(daoMock.count())
        .thenReturn(ProofOfMajorityAlgorithm.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM + 1);
    when(proOfMajorityMock.reachConsensus(any())).thenReturn(Optional.of(new Block()));

    factory.reachConsensus(new ArrayList<>());

    verify(powMock, times(0)).reachConsensus(any());
    verify(proOfMajorityMock, times(1)).reachConsensus(any());
  }

  @Test
  public void testReachConsensus_withConditionsMetForProofOfMajorityButItFails_shouldTryPoMConsensusAndFallbackToPoW() {
    when(statsMock.activeNodeCount()).thenReturn(101l);
    when(daoMock.count())
        .thenReturn(ProofOfMajorityAlgorithm.MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM + 1);
    when(proOfMajorityMock.reachConsensus(any())).thenThrow(ReachingConsentFailedException.class);

    factory.reachConsensus(new ArrayList<>());

    verify(powMock, times(1)).reachConsensus(any());
    verify(proOfMajorityMock, times(1)).reachConsensus(any());
  }

  @Test
  public void testStopFindingConsensus_shouldStopBoth() {
    factory.stopFindingConsensus();

    verify(powMock, times(1)).stopFindingConsensus();
    verify(proOfMajorityMock, times(1)).stopFindingConsensus();
  }
}
