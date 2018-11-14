package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.SyncInquirerImpl;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.commons.config.CommonsConfig;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;

@RunWith(SpringRunner.class)
@Import({TestConfig.class, CommonsConfig.class})
@ContextConfiguration(classes = {SyncInquirerImpl.class, MessagingUtils.class})
public class SyncInquirerTest2 {

  @MockBean(reset=MockReset.BEFORE)
  private Broadcaster<MessagePayload> broadcaster;
  @MockBean(reset=MockReset.BEFORE)
  private NetworkStatistics networkStatistics;
  @Autowired
  private MessagingUtils utils;
  
  @Autowired
  private SyncInquirer inquirer;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  
  private ExecutorService exec;
  
  @Before
  public void setup() {
    exec = Executors.newScheduledThreadPool(10);
  }
  
  @Test
  public void testFetchNextBatch_witOnlyTwoGoodResponses_shouldFetchAndPickEitherOne() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(20));
        
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(1,2)).thenReturn(fakeFuture(1,2)).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture())
    .thenReturn(exceptionalFuture()).thenReturn(exceptionalFuture());
    
    List<SyncResponse<Block>> responses = inquirer.fetchNextBatch(request, Block.class);
    assertFalse("verify response is there", responses.isEmpty());
    assertEquals("verify correct starting position", 2l, responses.get(0).getStartingPosition().longValue());
    assertNotNull("verify entities are not null", responses.get(0).getEntities());
    assertEquals("verify correct amount responded entities", 10, responses.get(0).getEntities().size());
    
    verify(broadcaster,times(6)).sendRequest(any(), anyString(), any(MainTopics.class));
    verify(networkStatistics, times(1)).activeNodeIds();
  }
  
  
  @Test
  public void testFetchNextBatch_withVeryFewNodes_shouldFetch() {
    SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(4l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(4));
        
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(1,2)).thenReturn(exceptionalFuture()).thenReturn(fakeFuture(1,2))
    .thenReturn(exceptionalFuture());
    
    
    List<SyncResponse<Block>> responses = inquirer.fetchNextBatch(request, Block.class);
    assertFalse("verify response is there", responses.isEmpty());
    assertEquals("verify correct starting position", 2l, responses.get(0).getStartingPosition().longValue());
    assertNotNull("verify entities are not null", responses.get(0).getEntities());
    assertEquals("verify correct amount responded entities", 10, responses.get(0).getEntities().size());
    
    verify(broadcaster,times(3)).sendRequest(any(), anyString(), any(MainTopics.class));
    verify(networkStatistics, times(1)).activeNodeIds();
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
