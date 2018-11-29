package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockStorageServiceImpl;
import com.flockinger.groschn.blockchain.config.CacheConfig;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.respond.BlockSyncInfoResponder;
import com.flockinger.groschn.blockchain.messaging.respond.BlockSyncResponder;
import com.flockinger.groschn.blockchain.messaging.sync.SmartBlockSynchronizer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.SmartBlockSynchronizerImpl;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.impl.InnerBlockValidator;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;
import com.flockinger.groschn.messaging.util.BeanValidator;
import com.flockinger.groschn.messaging.util.MessagingContext;
import com.google.common.collect.ImmutableList;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SmartBlockSynchronizerImpl.class,
    BlockSynchronizer.class, ScanningSyncStrategy.class,
    ScanResultMatcher.class, BlockInfoResultProvider.class, BlockChainSelector.class,
    BlockSyncResponder.class, BlockSyncInfoResponder.class, MessagingContext.class,
    MessagePackageHelper.class, BeanValidator.class,
    BlockStorageServiceImpl.class})
@Import(CacheConfig.class)
@TestPropertySource(properties = "atomix.node-id=bla123")
public class SmartBlockSynchronizerIntegrationTest extends BaseDbTest {

  @MockBean
  private BlockMaker makerMock;
  @MockBean
  private SyncInquirer inquirerMock;
  @MockBean
  private NetworkStatistics statisticsMock;
  @MockBean(name="innerBlockValidator")
  private InnerBlockValidator validatorMock;
  @MockBean
  private TransactionManager managerMock;
  @MockBean
  private BlockchainRepository daoMock;

  @Autowired
  private SmartBlockSynchronizer synchronizer;

  @Autowired
  private BlockSyncResponder blockResponder;
  @Autowired
  private BlockSyncInfoResponder infoResponder;
  @Autowired
  private MessagingContext utils;
  @MockBean
  private ClusterCommunicationService clusterCommunicationService;



  private ModelMapper mapper = new ModelMapper();

  @Before
  public void setup() throws Exception {

    when(statisticsMock.activeNodeIds()).thenReturn(ImmutableList.of("node1", "node2", "node3"));
    when(validatorMock.validate(any(Block.class))).thenReturn(Assessment.build().valid(true));
  }


  //TODO add many more tests!!


  @Test
  public void testScanSynchronize_withUpToDate_shouldCheckLastOneAndUpdate() throws Exception {
    prepareInquirerMock(3);
    when(daoMock.findByPositionBetweenInclusive(anyLong(), anyLong())).thenAnswer(new BlockAnswer(blocks(5)));
    when(daoMock.findFirstByOrderByPositionDesc()).thenReturn(Optional.of(blocks(5).get(4)));

    synchronizer.sync(5L);

    verify(makerMock).generation(eq(BlockMakerCommand.STOP));
    verify(makerMock).generation(eq(BlockMakerCommand.RESTART));
    verify(inquirerMock, times(1)).fetchNextBatch(any(),eq(Block.class));
    verify(inquirerMock, times(1 )).fetchNextBatch(any(),eq(BlockInfo.class));
    verify(statisticsMock, times(4)).activeNodeIds();
    verify(validatorMock, times(1)).validate(any());

    verify(daoMock, times(3 + (1 + 1)  + 1)).findByPositionBetweenInclusive(any(), any());
    verify(daoMock, times(3)).findFirstByOrderByPositionDesc();
  }

  @Test
  public void testScanSynchronize_withTwentyBlocksBehind_shouldSyncUpToLatest() throws Exception {
    prepareInquirerMock(1);
    when(daoMock.findByPositionBetweenInclusive(anyLong(), anyLong()))
        .thenAnswer(new BlockAnswer(blocks(25)))
        .thenAnswer(new BlockAnswer(blocks(5)))
        .thenAnswer(new BlockAnswer(blocks(5)))
        .thenAnswer(new BlockAnswer(blocks(25)))
        .thenAnswer(new BlockAnswer(blocks(5)))
        .thenAnswer(new BlockAnswer(blocks(5)))
        .thenAnswer(new BlockAnswer(blocks(25)))
        .thenAnswer(new BlockAnswer(blocks(5)))
        .thenAnswer(new BlockAnswer(blocks(5)))
        .thenAnswer(new BlockAnswer(blocks(25)))
        .thenAnswer(new BlockAnswer(blocks(25)))
        .thenAnswer(new BlockAnswer(blocks(25)));
    when(daoMock.findFirstByOrderByPositionDesc()).thenReturn(Optional.of(blocks(5).get(4)));

    synchronizer.sync(25L);

    verify(makerMock).generation(eq(BlockMakerCommand.STOP));
    verify(makerMock).generation(eq(BlockMakerCommand.RESTART));
    verify(inquirerMock, times(1 + 1 + 1)).fetchNextBatch(any(),eq(Block.class));
    verify(inquirerMock, times(1 + 1 + 1)).fetchNextBatch(any(),eq(BlockInfo.class));
    verify(statisticsMock, times(2 + 2 + 2)).activeNodeIds();
    verify(validatorMock, times(21)).validate(any());

    verify(daoMock, times(4 * 3)).findByPositionBetweenInclusive(any(), any());
    verify(daoMock, times(3)).findFirstByOrderByPositionDesc();
  }


  @Test
  public void testScanSynchronize_withFreshNode_shouldSyncUpToLatest() throws Exception {
    prepareInquirerMock(1);
    when(daoMock.findByPositionBetweenInclusive(anyLong(), anyLong()))
        .thenAnswer(new BlockAnswer(blocks(9)))
        .thenAnswer(new BlockAnswer(blocks(1)))
        .thenAnswer(new BlockAnswer(blocks(1)))
        .thenAnswer(new BlockAnswer(blocks(9)))
        .thenAnswer(new BlockAnswer(blocks(1)))
        .thenAnswer(new BlockAnswer(blocks(1)))
        .thenAnswer(new BlockAnswer(blocks(9)));
    when(daoMock.findFirstByOrderByPositionDesc()).thenReturn(Optional.of(blocks(1).get(0)));

    synchronizer.sync(9L);

    verify(makerMock).generation(eq(BlockMakerCommand.STOP));
    verify(makerMock).generation(eq(BlockMakerCommand.RESTART));
    verify(inquirerMock, times(1  )).fetchNextBatch(any(),eq(Block.class));
    verify(inquirerMock, times(1 + 1 )).fetchNextBatch(any(),eq(BlockInfo.class));
    verify(statisticsMock, times(1 + 1 + 1 )).activeNodeIds();
    verify(validatorMock, times(8)).validate(any());

    verify(daoMock, times(7)).findByPositionBetweenInclusive(any(), any());
    verify(daoMock, times(2)).findFirstByOrderByPositionDesc();
  }

  @Test
  public void testScanSynchronize_withSomeCorruptBlocks_shouldFixSyncUpToLatest() throws Exception {
    prepareInquirerMock(2);
    when(daoMock.findByPositionBetweenInclusive(anyLong(), anyLong()))
        .thenAnswer(new BlockAnswer(blocks(9)))
        .thenAnswer(new BlockAnswer(blocks(9)))
        .thenAnswer(new BlockAnswer(faultyBlocks(9, 4)))
        .thenAnswer(new BlockAnswer(faultyBlocks(9, 4)))
        .thenAnswer(new BlockAnswer(blocks(9)))
        .thenAnswer(new BlockAnswer(blocks(9)))
        .thenAnswer(new BlockAnswer(faultyBlocks(9, 4)))
        .thenAnswer(new BlockAnswer(faultyBlocks(9, 4)))
        .thenAnswer(new BlockAnswer(blocks(9)));
    when(daoMock.findFirstByOrderByPositionDesc()).thenReturn(Optional.of(blocks(9).get(0)));

    synchronizer.sync(9L);

    verify(makerMock).generation(eq(BlockMakerCommand.STOP));
    verify(makerMock).generation(eq(BlockMakerCommand.RESTART));
    verify(inquirerMock, times(1  )).fetchNextBatch(any(),eq(Block.class));
    verify(inquirerMock, times(1 + 1 )).fetchNextBatch(any(),eq(BlockInfo.class));
    verify(statisticsMock, times(2*(1 + 1) + 1 )).activeNodeIds();
    verify(validatorMock, times(6)).validate(any());

    verify(daoMock, times(9)).findByPositionBetweenInclusive(any(), any());
    verify(daoMock, times(4)).findFirstByOrderByPositionDesc();
  }



  private void prepareInquirerMock(final int amountOfInfoReceivers) {
    when(inquirerMock.fetchNextBatch(any(), any())).thenAnswer((Answer) invocation -> {
      var type = (Class<?>)invocation.getArgument(1);
      if(type.getSimpleName().contains("BlockInfo")) {
        return IntStream.range(0,amountOfInfoReceivers)
            .mapToObj(it -> utils.extractPayload(infoResponder.respond(createMessage((SyncBatchRequest) invocation.getArgument(0))), SyncResponse.class).get())
            .collect(Collectors.toList());
      } else {
        return new ArrayList<>(ImmutableList.of( utils.extractPayload(blockResponder.respond(createMessage((SyncBatchRequest) invocation.getArgument(0))), SyncResponse.class).get()));
      }
    });
  }

  private static class BlockAnswer implements Answer<List<StoredBlock>> {
    private List<StoredBlock> blocks;
    public BlockAnswer(List<StoredBlock> blocks) {
      this.blocks = blocks;
    }
    @Override
    public List<StoredBlock> answer(InvocationOnMock invocation) throws Throwable {
      final int ZERO_BASE_SHIFT = 1;
      Long from =  ((Long)invocation.getArgument(0)) - ZERO_BASE_SHIFT;
      Long until =  ((Long)invocation.getArgument(1)) - ZERO_BASE_SHIFT;
      final int EXCLUSIFE_SHIFT = 1;
      // @Query("{\"position\" : {\"$gte\" : ?0, \"$lte\" : ?1}}")
      if(from >= blocks.size()) {
        return new ArrayList<>();
      } else  {
        return blocks.subList(from.intValue(), Math.min(blocks.size(), until.intValue() + EXCLUSIFE_SHIFT));
      }
    }
  }

  private List<StoredBlock> blocks(int amount) throws Exception {
    return TestDataFactory.fakeBlocks(amount, 1, "hash").stream()
        .map(it ->  mapper.map(it, StoredBlock.class)).collect(Collectors.toList());
  }

  private List<StoredBlock> faultyBlocks(int amount, int faultyFrom) throws Exception {
    var faultyBlocks =  blocks(amount);

    IntStream.range(faultyFrom , amount).forEach( count ->
        faultyBlocks.get(count).setHash(faultyBlocks.get(count).getHash().replace("hash","hish")));

    return faultyBlocks;
  }


  private Message<MessagePayload> createMessage(SyncBatchRequest request) {
    Message<MessagePayload> message = new Message<>();
    message.setPayload(createPayload(request));
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(new Date().getTime() + 1L);
    return message;
  }

  private MessagePayload createPayload(SyncBatchRequest batchRequest) {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(batchRequest.getFromPosition());
    request.setRequestPackageSize(Integer.toUnsignedLong(batchRequest.getBatchSize()));
    request.setWantedHeaders(batchRequest.getWantedHeaders());
    return utils.packageMessage(request, "node1").getPayload();
  }

}
