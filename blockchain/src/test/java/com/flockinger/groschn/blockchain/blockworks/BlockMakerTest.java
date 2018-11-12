package com.flockinger.groschn.blockchain.blockworks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Optional;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
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

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockMakerImpl.class, MessagingUtils.class})
@Import(TestConfig.class)
public class BlockMakerTest extends BaseCachingTest {

  @MockBean(reset=MockReset.BEFORE)
  private ConsensusFactory consensusFactory;
  @MockBean(reset=MockReset.BEFORE)
  private TransactionManager transactionManager;
  @MockBean(reset=MockReset.BEFORE)
  private BlockStorageService storageService;
  @MockBean(reset=MockReset.BEFORE)
  private Broadcaster<MessagePayload> broadcaster;

  @MockBean(reset=MockReset.BEFORE)
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
    when(consensusFactory.reachConsensus(anyList())).thenReturn(Optional.of(new Block()));


    maker.generation(BlockMakerCommand.RESTART);
    
    verify(transactionManager, timeout(500)).fetchTransactionsBySize(anyLong());
    verify(broadcaster, timeout(500).times(1)).broadcast(any(), any());
    verify(storageService, timeout(500).times(1)).saveInBlockchain(any());
    verify(rewardGenerator, timeout(500)).generateRewardTransaction(any());
    verify(consensusFactory, timeout(500).times(1)).reachConsensus(any());
  }
  
  @Test
  public void testGenerationRestart_withEmptyConsensusResponse_shouldDoNothing()
      throws InterruptedException {
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    when(consensusFactory.reachConsensus(anyList())).thenReturn(Optional.empty());


    maker.generation(BlockMakerCommand.RESTART);

    verify(transactionManager, timeout(500)).fetchTransactionsBySize(anyLong());
    verify(rewardGenerator, timeout(500)).generateRewardTransaction(any());
    verify(consensusFactory, timeout(500).times(1)).reachConsensus(any());
    verify(broadcaster, never()).broadcast(any(), any());
    verify(storageService, never()).saveInBlockchain(any());
  }


  @Test
  public void testGenerationRestart_withStorageServiceThrowingException_shouldDoNothing() throws InterruptedException {
    when(consensusFactory.reachConsensus(anyList())).thenReturn(Optional.of(new Block()));
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    when(storageService.saveInBlockchain(any())).thenThrow(BlockValidationException.class);

    maker.generation(BlockMakerCommand.RESTART);
    Thread.sleep(200);
  }

  @Test
  public void testGenerationRestart_withConsensusFactoryThrowingException_shouldDoNothing() throws InterruptedException {
    when(consensusFactory.reachConsensus(anyList())).thenThrow(ReachingConsentFailedException.class);
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());

    maker.generation(BlockMakerCommand.RESTART);
    Thread.sleep(200);
  }

  @Test
  public void testGenerateStop_withRunningGeneration_shouldStop() {
    when(consensusFactory.reachConsensus(anyList())).thenAnswer(new AnswersWithDelay(10000, null));
    
    maker.generation(BlockMakerCommand.STOP);
    
    verify(consensusFactory, timeout(500)).stopFindingConsensus();
  }

  @Test
  public void testStatus_withStoppedGen_shouldReturnCorrect() throws InterruptedException {
    when(consensusFactory.reachConsensus(anyList())).thenAnswer(new AnswersWithDelay(10000, null));
    
    maker.generation(BlockMakerCommand.STOP);
    
    Awaitility.await().until(() -> BlockGenerationStatus.STOPPED.equals(maker.status()));
  }
  
  @Test
  public void testStatus_withStartedGen_shouldReturnCorrect() {
    when(consensusFactory.reachConsensus(anyList())).thenAnswer(new AnswersWithDelay(10000, null));
    
    maker.generation(BlockMakerCommand.RESTART);
    
    Awaitility.await().until(() -> BlockGenerationStatus.RUNNING.equals(maker.status()));
  }
  
  @Test
  public void testStatus_withCompletedGen_shouldReturnCorrect() {
    when(consensusFactory.reachConsensus(anyList()))
         .thenReturn(Optional.of(new Block()));
    
    maker.generation(BlockMakerCommand.RESTART);
    
    Awaitility.await().until(() -> BlockGenerationStatus.COMPLETE.equals(maker.status()));
  }
}
