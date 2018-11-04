package com.flockinger.groschn.blockchain.blockworks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
  public void testProduceBlock_withValidTransactions_shouldCreateStoreAndRewardBlock() {
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    when(consensusFactory.reachConsensus(anyList())).thenReturn(new Block());

    maker.produceBlock();

    verify(transactionManager).fetchTransactionsBySize(anyLong());
    verify(broadcaster, times(1)).broadcast(any(), any());
    verify(storageService).saveInBlockchain(any());
    verify(rewardGenerator).generateRewardTransaction(any());
    verify(consensusFactory, times(1)).reachConsensus(any());
  }


  @Test
  public void testProduceBlock_withStorageServiceThrowingException_shouldDoNothing() {
    when(consensusFactory.reachConsensus(anyList())).thenReturn(new Block());
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    when(storageService.saveInBlockchain(any())).thenThrow(BlockValidationException.class);

    maker.produceBlock();
  }

  @Test
  public void testProduceBlock_withConsensusFactoryThrowingException_shouldDoNothing() {
    when(consensusFactory.reachConsensus(anyList()))
        .thenThrow(ReachingConsentFailedException.class);
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());

    maker.produceBlock();
  }
}
