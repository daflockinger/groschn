package com.flockinger.groschn.blockchain.blockworks;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockGenerationStatus;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockMakerImpl;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.exception.ReachingConsentFailedException;
import com.flockinger.groschn.blockchain.exception.validation.BlockValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockMakerImpl.class, MessagingUtils.class})
@Import(TestConfig.class)
public class BlockMakerTest extends BaseCachingTest {

  @MockBean
  private ConsensusFactory consensusFactory;
  @MockBean
  private TransactionManager transactionManager;
  @MockBean
  private BlockStorageService storageService;
  @MockBean
  private Broadcaster<MessagePayload> broadcaster;

  @MockBean
  private RewardGenerator rewardGenerator;

  @Autowired
  private BlockMaker maker;

  @Before
  public void setup() {
    when(storageService.getLatestBlock()).thenReturn(Block.GENESIS_BLOCK());
    when(rewardGenerator.generateRewardTransaction(anyList())).thenReturn(new ArrayList<>());
  }

  @Test
  public void testGenerationRestart_withValidTransactions_shouldCreateStoreAndRewardBlock()
      throws InterruptedException {
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    when(consensusFactory.reachConsensus(anyList())).thenReturn(Mono.just(new Block()));


    maker.generation(BlockMakerCommand.RESTART);

    StepVerifier.setDefaultTimeout(Duration.ofMillis(200l));

    verify(transactionManager).fetchTransactionsBySize(anyLong());
    verify(broadcaster, times(1)).broadcast(any(), any());
    verify(storageService).saveInBlockchain(any());
    verify(rewardGenerator).generateRewardTransaction(any());
    verify(consensusFactory, times(1)).reachConsensus(any());
  }


  @Test
  public void testGenerationRestart_withStorageServiceThrowingException_shouldDoNothing() {
    when(consensusFactory.reachConsensus(anyList())).thenReturn(Mono.just(new Block()));
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    when(storageService.saveInBlockchain(any())).thenThrow(BlockValidationException.class);

    maker.generation(BlockMakerCommand.RESTART);
  }

  @Test
  public void testGenerationRestart_withConsensusFactoryThrowingException_shouldDoNothing() {
    when(consensusFactory.reachConsensus(anyList()))
        .thenReturn(Mono.error(new ReachingConsentFailedException("")));
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());

    maker.generation(BlockMakerCommand.RESTART);
  }

  @Test
  public void testGenerateStop_withRunningGeneration_shouldStop() {
    maker.generation(BlockMakerCommand.STOP);
    verify(consensusFactory).stopFindingConsensus();
  }

  @Test
  public void testStatus_withStoppedGen_shouldReturnCorrect() {
    maker.generation(BlockMakerCommand.STOP);
    
    assertEquals("verify correct stop status", BlockGenerationStatus.STOPPED, maker.status());
  }
  
  @Test
  public void testStatus_withStartedGen_shouldReturnCorrect() {
    when(consensusFactory.reachConsensus(anyList()))
         .thenReturn(Mono.delay(Duration.ofMillis(200l)).map(d -> new Block()));
    
    maker.generation(BlockMakerCommand.RESTART);
    
    assertEquals("verify correct running status", BlockGenerationStatus.RUNNING, maker.status());
  }
  
  @Test
  public void testStatus_withCompletedGen_shouldReturnCorrect() {
    when(consensusFactory.reachConsensus(anyList()))
         .thenReturn(Mono.just(new Block()));
    
    maker.generation(BlockMakerCommand.RESTART);
    
    assertEquals("verify correct completed status", BlockGenerationStatus.COMPLETE, maker.status());
  }
}
