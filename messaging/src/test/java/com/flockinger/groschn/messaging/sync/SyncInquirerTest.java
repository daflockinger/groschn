package com.flockinger.groschn.messaging.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.messaging.ExecutorConfig;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.exception.ReceivedMessageInvalidException;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.util.BeanValidator;
import com.flockinger.groschn.messaging.util.TestBlock;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@Import({ExecutorConfig.class})
@ContextConfiguration(classes = {SyncInquirerImpl.class, BeanValidator.class})
public class SyncInquirerTest {

  @MockBean(reset=MockReset.BEFORE)
  private SyncRequester requester;
  @MockBean(reset=MockReset.BEFORE)
  private NetworkStatistics networkStatistics;
  @MockBean
  private ConcurrentMessenger messengerMock;

  @Autowired
  private BeanValidator utils;

  @Autowired
  private SyncInquirer inquirer;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  
  @Test
  public void testFetchNextBatch_withLotsOfNodesAndNiceMessages_shouldReturnCorrect() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds("id",20));
    when(messengerMock.fetch(any(), any())).thenReturn(ImmutableList.of(fakeFuture(1,2),fakeFuture(1,2),fakeFuture(1,2)
    ,fakeFuture(20,2),fakeFuture(90,2),fakeFuture(20,2)));
    
    
    List<SyncResponse<TestBlock>> responses = inquirer.fetchNextBatch(request, TestBlock.class);
    assertFalse("verify responses are there", responses.isEmpty());
    assertEquals("verify correct starting position", 2l, responses.get(0).getStartingPosition().longValue());
    assertNotNull("verify entities are not null", responses.get(0).getEntities());
    assertEquals("verify correct amount responded entities", 10, responses.get(0).getEntities().size());
    
   
    verify(messengerMock,times(1)).fetch(any(), any());    
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  
  @Test
  public void testFetchNextBatch_withMessageFetchingReturnsEmptyTwoTimes_shouldRetry() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds("id",20));
    when(messengerMock.fetch(any(), any()))
    .thenReturn(new ArrayList<>())
    .thenReturn(new ArrayList<>())
    .thenReturn(ImmutableList.of(fakeFuture(1,2),fakeFuture(1,2),fakeFuture(1,2)
    ,fakeFuture(20,2),fakeFuture(90,2),fakeFuture(20,2)));
    
    
    List<SyncResponse<TestBlock>> responses = inquirer.fetchNextBatch(request, TestBlock.class);
    assertFalse("verify responses are there", responses.isEmpty());
    assertEquals("verify correct starting position", 2l, responses.get(0).getStartingPosition().longValue());
    assertNotNull("verify entities are not null", responses.get(0).getEntities());
    assertEquals("verify correct amount responded entities", 10, responses.get(0).getEntities().size());
    
   
    verify(messengerMock,times(3)).fetch(any(), any());    
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  @Test
  public void testFetchNextBatch_withMessageFetchingReturnsEmptyAlways_shouldReturnEmpty() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds("id",20));
    when(messengerMock.fetch(any(), any()))
    .thenReturn(new ArrayList<>());
    
    List<SyncResponse<TestBlock>> responses = inquirer.fetchNextBatch(request, TestBlock.class);
    assertTrue("verify responses are empty", responses.isEmpty());
   
    verify(messengerMock,times(3)).fetch(any(), any());    
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  @Test
  public void testFetchNextBatch_withUsingSelectedNodeIdsAndHeaders_shouldSendOnlyToSelectedOnes() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN)
        .selectedNodeIds(generateNodeIds("selected", 10));
    request.getSelectedNodeIds().remove(nodeId);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds("id",20));
    when(messengerMock.fetch(any(), any())).thenReturn(ImmutableList.of(fakeFuture(1,2),fakeFuture(1,2),fakeFuture(1,2)
    ,fakeFuture(20,2),fakeFuture(90,2),fakeFuture(20,2)));
    
    
    List<SyncResponse<TestBlock>> responses = inquirer.fetchNextBatch(request, TestBlock.class);
    assertFalse("verify responses are there", responses.isEmpty());
    assertEquals("verify correct starting position", 2l, responses.get(0).getStartingPosition().longValue());
    assertNotNull("verify entities are not null", responses.get(0).getEntities());
    assertEquals("verify correct amount responded entities", 10, responses.get(0).getEntities().size());
    
    ArgumentCaptor<List> requestsCaptor = ArgumentCaptor.forClass(List.class);
    verify(messengerMock,times(1)).fetch(requestsCaptor.capture(), any());
    var sentRequests = requestsCaptor.getAllValues().stream()
        .map(it -> (List<Entry<String, SyncBatchRequest>>)it).collect(Collectors.toList());
    boolean areRequestSentOnlyToSelected = sentRequests.stream()
        .flatMap(Collection::stream)
        .map(Entry::getKey)
        .allMatch(id -> id.startsWith("selected"));
    assertTrue("verify all sent requests are only sent to selected Nodes", areRequestSentOnlyToSelected);
    verify(networkStatistics, never()).activeNodeIds();
  }

  @Test
  public void testFetchNextBatch_shouldHaveAlwaysDifferentNodeIds() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(100l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds("id",100));
    when(messengerMock.fetch(any(), any())).thenReturn(ImmutableList.of(fakeFuture(1,2),fakeFuture(1,9, 15, false),fakeFuture(1,2)
        ,fakeFuture(10,13),fakeFuture(10,15),fakeFuture(10,19)));
    
    inquirer.fetchNextBatch(request, TestBlock.class);
    inquirer.fetchNextBatch(request, TestBlock.class);
    inquirer.fetchNextBatch(request, TestBlock.class);
    inquirer.fetchNextBatch(request, TestBlock.class);
    
    ArgumentCaptor<List> requestsCaptor = ArgumentCaptor.forClass(List.class);
    verify(messengerMock,times(4)).fetch(requestsCaptor.capture(), any());
    var sentRequests = requestsCaptor.getAllValues().stream()
        .map(it -> (List<Entry<String, SyncBatchRequest>>)it).collect(Collectors.toList());
    var alwaysSimmilarPickedNodesCount = sentRequests.stream()
        .flatMap(Collection::stream)
        .map(Entry::getKey)
        .collect(Collectors.groupingBy(String::toString,Collectors.counting()))
        .values().stream().filter(simmliarPickedNodesCount -> simmliarPickedNodesCount ==4).count();
    assertTrue("verify that different nodes are picked each time, kinda", alwaysSimmilarPickedNodesCount < 3);
  }
  
    
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withZeroBatchSize_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(0)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    
    inquirer.fetchNextBatch(request, TestBlock.class);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withZeroIdealNodeCount_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(0).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    
    inquirer.fetchNextBatch(request, TestBlock.class);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withNullTopic_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(null);
    request.setFromPosition(2);
    
    inquirer.fetchNextBatch(request, TestBlock.class);
  }
  
  @Test(expected=ReceivedMessageInvalidException.class)
  public void testFetchNextBatch_withZeroStartPosition_shouldThrowException() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(0);
    
    inquirer.fetchNextBatch(request, TestBlock.class);
  }
  
  
  private  Optional<SyncResponse<TestBlock>> fakeFuture(int timeout, long startPosition) {
    return fakeFuture(timeout, startPosition, 10, false);
  }
  
  private Optional<SyncResponse<TestBlock>> fakeFuture(int timeout, long startPosition, int responseBlockAmount, boolean lastPosReached) {
      var response = new SyncResponse<TestBlock>();
      response.setStartingPosition(startPosition);
      response.setEntities(getBlockAmount(responseBlockAmount));
      response.setLastPositionReached(lastPosReached);
      try {
        Thread.sleep(timeout);
      } catch (InterruptedException e) {}
      return Optional.of(response);
  }
  
  private List<TestBlock> getBlockAmount(int amount) {
    return IntStream.range(0, amount).mapToObj(am -> new TestBlock()).collect(Collectors.toList());
  }
  
  private List<String> generateNodeIds(String prefix, int amount) {
    List<String> nodes = new ArrayList<>();
    nodes.add(nodeId);
    IntStream.range(0, amount - 1).forEach(a -> nodes.add(prefix + Integer.toString(a)));
    return nodes;
  }
}
