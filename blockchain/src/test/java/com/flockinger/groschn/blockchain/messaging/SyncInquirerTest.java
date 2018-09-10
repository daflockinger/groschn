package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.SyncInquirerImpl;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.blockchain.util.serialize.impl.FstSerializer;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.outbound.Broadcaster;

@ContextConfiguration(classes = {SyncInquirerImpl.class, MessageReceiverUtils.class, CompressionUtils.class, FstSerializer.class})
public class SyncInquirerTest extends BaseCachingTest{

  @MockBean
  private Broadcaster<MessagePayload> broadcaster;
  @MockBean
  private NetworkStatistics networkStatistics;
  @MockBean
  private MerkleRootCalculator merkleCalculator;
  @Autowired
  private MessageReceiverUtils utils;
  
  @Autowired
  private SyncInquirer inquirer;
  
  @Value("${blockchain.node.id}")
  private String nodeId;
  
  @Test
  public void testFetchNextBatch_withValidRequestNoRetries_shouldFetch() {
    /*SyncBatchRequest request = SyncBatchRequest.build().batchSize(10)
        .idealReceiveNodeCount(3).maxFetchRetries(3).topic(MainTopics.SYNC_BLOCKCHAIN);
    request.setFromPosition(2);
    when(networkStatistics.activeNodeCount()).thenReturn(20l);
    when(networkStatistics.activeNodeIds()).thenReturn(generateNodeIds(20));
    when(merkleCalculator.calculateMerkleRootHash(any())).thenReturn("realOne")
        .thenReturn("fakeOne").thenReturn("realOne");
    
    when(broadcaster.sendRequest(any(), anyString(), any(MainTopics.class)))
    .thenReturn(fakeFuture(1)).thenReturn(fakeFuture(1)).thenReturn(fakeFuture(1))
    .thenReturn(fakeFuture(10)).thenReturn(fakeFuture(10)).thenReturn(fakeFuture(10));
    
    
    Optional<SyncResponse<Block>> response = inquirer.fetchNextBatch(request, Block.class);
    assertTrue("verify response is there", response.isPresent());
    assertEquals("verify correct starting position", 2l, response.get().getStartingPosition().longValue());
    assertNotNull("verify entities are not null", response.get().getEntities());
    assertEquals("verify correct amount responded entities", 10, response.get().getEntities().size());*/
    
  }
  
  
  
  private CompletableFuture<Message<MessagePayload>> fakeFuture(int timeout) {
    //TODO create message and continue
    return null;
  }
  
  private List<String> generateNodeIds(int amount) {
    List<String> nodes = new ArrayList<>();
    nodes.add(nodeId);
    IntStream.range(0, amount - 1).forEach(a -> nodes.add(UUID.randomUUID().toString()));
    return nodes;
  }
  
}
