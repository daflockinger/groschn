package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.SyncInquirerImpl;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.commons.MerkleRootCalculator;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.exception.ReceivedMessageInvalidException;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;

@ContextConfiguration(classes = {SyncInquirerImpl.class, MessagingUtils.class})
public class SyncInquirerTest extends BaseCachingTest{

  @MockBean(reset=MockReset.BEFORE)
  private Broadcaster<MessagePayload> broadcaster;
  @MockBean(reset=MockReset.BEFORE)
  private NetworkStatistics networkStatistics;
  @MockBean(reset=MockReset.BEFORE)
  private MerkleRootCalculator merkleCalculator;
  @Autowired
  private MessagingUtils utils;
  
  @Autowired
  private SyncInquirer inquirer;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  ExecutorService exec = Executors.newScheduledThreadPool(10);
  
  
  
  @Test
  public void testFetchNextBatch_withValidRequestNoRetries_shouldFetch() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(20));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        var blocks = (List<Block>)invocation.getArgument(0);
        return blocks.size() != 10 ? "fake" : "realOne";
      }});
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(1,2)).thenReturn(fakeFuture(1,9, 15, false)).thenReturn(fakeFuture(1,2))
    .thenReturn(fakeFuture(200,13)).thenReturn(fakeFuture(200,15)).thenReturn(fakeFuture(200,19));
    
    
    Optional<SyncResponse<Block>> response = inquirer.fetchNextBatch(request, Block.class);
    assertTrue("verify response is there", response.isPresent());
    assertEquals("verify correct starting position", 2l, response.get().getStartingPosition().longValue());
    assertNotNull("verify entities are not null", response.get().getEntities());
    assertEquals("verify correct amount responded entities", 10, response.get().getEntities().size());
    
    var requestCaptor = ArgumentCaptor.forClass(Message.class);
   
    verify(broadcaster,times(6)).sendRequest(requestCaptor.capture(), anyString(), any(MainTopics.class));
    var bla = requestCaptor.getAllValues();
    Optional<SyncRequest> firstSentSyncReq = utils.extractPayload((Message<MessagePayload>)requestCaptor.getAllValues().get(0), SyncRequest.class);
    assertTrue("verify non null request is broadcasted", firstSentSyncReq.isPresent());
    assertEquals("verify non null request has correct start position", 2l, 
        firstSentSyncReq.get().getStartingPosition().longValue());
    assertEquals("verify non null request has correct batch size", 10l, 
        firstSentSyncReq.get().getRequestPackageSize().longValue());
    
    verify(merkleCalculator, atLeast(3)).calculateMerkleRootHash(any());
    verify(networkStatistics, times(1)).activeNodeCount();
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  @Test
  public void testFetchNextBatch_withMoreValidCompetingResults_shouldFetchAndPickMajorityOne() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(20));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        var blocks = (List<Block>)invocation.getArgument(0);
        return blocks.size() == 10 ? "twooiis" : "threeiis";
      }});
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(10,2)).thenReturn(fakeFuture(10,3, 14, false)).thenReturn(fakeFuture(10,3, 14, false))
    .thenReturn(fakeFuture(1,2)).thenReturn(fakeFuture(10,2)).thenReturn(exceptionalFuture());
    
    
    Optional<SyncResponse<Block>> response = inquirer.fetchNextBatch(request, Block.class);
    assertTrue("verify response is there", response.isPresent());
    assertEquals("verify correct starting position", 2l, response.get().getStartingPosition().longValue());
    assertNotNull("verify entities are not null", response.get().getEntities());
    assertEquals("verify correct amount responded entities", 10, response.get().getEntities().size());
    
    verify(broadcaster, times(6)).sendRequest(any(), anyString(), any(MainTopics.class));
    verify(merkleCalculator, atLeast(3)).calculateMerkleRootHash(any());
    verify(networkStatistics, times(1)).activeNodeCount();
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  @Test
  public void testFetchNextBatch_witOnlyTwoGoodResponses_shouldFetchAndPickEitherOne() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(20));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        var blocks = (List<Block>)invocation.getArgument(0);
        return blocks.size() != 10 ? "fake" : "realOne";
      }});
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(1,2)).thenReturn(fakeFuture(1,2)).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture());
    
    Optional<SyncResponse<Block>> response = inquirer.fetchNextBatch(request, Block.class);
    assertTrue("verify response is there", response.isPresent());
    assertEquals("verify correct starting position", 2l, response.get().getStartingPosition().longValue());
    assertNotNull("verify entities are not null", response.get().getEntities());
    assertEquals("verify correct amount responded entities", 10, response.get().getEntities().size());
    
    verify(broadcaster,times(6)).sendRequest(any(), anyString(), any(MainTopics.class));
    verify(merkleCalculator, times(2)).calculateMerkleRootHash(any());
    verify(networkStatistics, times(1)).activeNodeCount();
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  @Test
  public void testFetchNextBatch_witOnlyOneResponses_shouldFetchAndPickThatOne() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(20));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        var blocks = (List<Block>)invocation.getArgument(0);
        return blocks.size() != 10 ? "fake" : "realOne";
      }});
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(exceptionalFuture()).thenReturn(fakeFuture(1,2)).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture());
    
    Optional<SyncResponse<Block>> response = inquirer.fetchNextBatch(request, Block.class);
    assertTrue("verify response is there", response.isPresent());
    assertEquals("verify correct starting position", 2l, response.get().getStartingPosition().longValue());
    assertNotNull("verify entities are not null", response.get().getEntities());
    assertEquals("verify correct amount responded entities", 10, response.get().getEntities().size());
    
    verify(broadcaster,times(6)).sendRequest(any(), anyString(), any(MainTopics.class));
    verify(merkleCalculator, times(1)).calculateMerkleRootHash(any());
    verify(networkStatistics, times(1)).activeNodeCount();
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  
  @Test
  public void testFetchNextBatch_withFirstAndSecondAttemptReturnedEmptyResponsesShouldRetry_shouldFetch() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(20));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenReturn("realOne")
        .thenReturn("fakeOne").thenReturn("realOne");
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1))
    .thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1))
    .thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1))
    .thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1)).thenReturn(emptyFuture(1))
    .thenReturn(fakeFuture(1,2)).thenReturn(fakeFuture(1,2)).thenReturn(fakeFuture(1,2))
    .thenReturn(fakeFuture(100,2)).thenReturn(fakeFuture(100,2)).thenReturn(fakeFuture(100,2));
    
    Optional<SyncResponse<Block>> response = inquirer.fetchNextBatch(request, Block.class);
    assertTrue("verify response is there", response.isPresent());
    assertEquals("verify correct starting position", 2l, response.get().getStartingPosition().longValue());
    assertNotNull("verify entities are not null", response.get().getEntities());
    assertEquals("verify correct amount responded entities", 10, response.get().getEntities().size());
    
    verify(broadcaster,times(18)).sendRequest(any(), anyString(), any(MainTopics.class));
    verify(merkleCalculator, atLeast(3)).calculateMerkleRootHash(any());
    verify(networkStatistics, times(3)).activeNodeCount();
    verify(networkStatistics, times(3)).activeNodeIds();
  }
  
  
  @Test
  public void testFetchNextBatch_shouldHaveAlwaysDifferentNodeIds() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(100l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(100));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        var blocks = (List<Block>)invocation.getArgument(0);
        return blocks.size() != 10 ? "fake" : "realOne";
      }});
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(1,2)).thenReturn(fakeFuture(1,9, 15, false)).thenReturn(fakeFuture(1,2))
    .thenReturn(fakeFuture(100,13)).thenReturn(fakeFuture(100,15)).thenReturn(fakeFuture(100,19));
    
    inquirer.fetchNextBatch(request, Block.class);
    inquirer.fetchNextBatch(request, Block.class);
    inquirer.fetchNextBatch(request, Block.class);
    inquirer.fetchNextBatch(request, Block.class);
    
    var receiverNodeCaptor = ArgumentCaptor.forClass(String.class);
    verify(broadcaster, atLeast(18)).sendRequest(any(), receiverNodeCaptor.capture(), any());
    var alwaysSimmilarPickedNodesCount = receiverNodeCaptor.getAllValues().stream()
        .collect(Collectors.groupingBy(String::toString,Collectors.counting()))
        .values().stream().filter(simmliarPickedNodesCount -> simmliarPickedNodesCount ==4).count();
    assertTrue("verify that different nodes are picked each time, kinda", alwaysSimmilarPickedNodesCount < 3);
  }
  
  
  @Test
  public void testFetchNextBatch_withVeryFewNodes_shouldFetch() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(4l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(4));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        var blocks = (List<Block>)invocation.getArgument(0);
        return blocks.size() != 10 ? "fake" : "realOne";
      }});
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(1,2)).thenReturn(exceptionalFuture()).thenReturn(fakeFuture(1,2));
    
    
    Optional<SyncResponse<Block>> response = inquirer.fetchNextBatch(request, Block.class);
    assertTrue("verify response is there", response.isPresent());
    assertEquals("verify correct starting position", 2l, response.get().getStartingPosition().longValue());
    assertNotNull("verify entities are not null", response.get().getEntities());
    assertEquals("verify correct amount responded entities", 10, response.get().getEntities().size());
    
    verify(broadcaster,times(3)).sendRequest(any(), anyString(), any(MainTopics.class));
    verify(merkleCalculator, times(2)).calculateMerkleRootHash(any());
    verify(networkStatistics, times(1)).activeNodeCount();
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withZeroBatchSize_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(0)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    
    inquirer.fetchNextBatch(request, Block.class);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withZeroIdealNodeCount_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(0).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    
    inquirer.fetchNextBatch(request, Block.class);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withNullTopic_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(null);
    request.setFromPosition(2);
    
    inquirer.fetchNextBatch(request, Block.class);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withZeroStartPosition_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(0);
    
    inquirer.fetchNextBatch(request, Block.class);
  }
  
  private CompletableFuture<Message<MessagePayload>> fakeFuture(int timeout, long startPosition) {
    return fakeFuture(timeout, startPosition, 10, false);
  }
  
  private CompletableFuture<Message<MessagePayload>> fakeFuture(int timeout, long startPosition, int responseBlockAmount, boolean lastPosReached) {
    return CompletableFuture.supplyAsync(() -> {
      var response = new SyncResponse<Block>();
      response.setStartingPosition(startPosition);
      response.setEntities(getBlockAmount(responseBlockAmount));
      response.setLastPositionReached(lastPosReached);
      var message = utils.packageMessage(response, UUID.randomUUID().toString());
      try {
        Thread.sleep(timeout * 100);
      } catch (InterruptedException e) {}
      return message;
    },exec);
  }
  
  private CompletableFuture<Message<MessagePayload>> emptyFuture(int timeout) {
    return CompletableFuture.supplyAsync(() -> {
      var message = utils.packageMessage(null, UUID.randomUUID().toString());
      try {
        Thread.sleep(timeout * 100);
      } catch (InterruptedException e) {}
      return message;
    },exec);
  }
  
  private CompletableFuture<Message<MessagePayload>> exceptionalFuture() {
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException();
    },exec);
  }
  
  private List<Block> getBlockAmount(int amount) {
    return IntStream.range(0, amount).mapToObj(am -> new Block()).collect(Collectors.toList());
  }
  
  private List<String> generateNodeIds(int amount) {
    List<String> nodes = new ArrayList<>();
    nodes.add(nodeId);
    IntStream.range(0, amount - 1).forEach(a -> nodes.add(Integer.toString(a)));
    return nodes;
  }
  
}
