package com.flockinger.groschn.blockchain.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.messaging.sync.ConcurrentMessenger;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.SyncInquirerImpl;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.commons.config.CommonsConfig;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;

@RunWith(SpringRunner.class)
@Import({TestConfig.class, CommonsConfig.class})
@ContextConfiguration(classes = {SyncInquirerImpl.class, MessagingUtils.class})
public class SyncInquirerTest {

  @MockBean(reset=MockReset.BEFORE)
  private Broadcaster<MessagePayload> broadcaster;
  @MockBean(reset=MockReset.BEFORE)
  private NetworkStatistics networkStatistics;
  @MockBean
  private ConcurrentMessenger messengerMock;
  @Autowired
  private MessagingUtils utils;
 
  
  @Autowired
  private SyncInquirer inquirer;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  
  
  
  
  
  
  
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
    });
  }
  
  private CompletableFuture<Message<MessagePayload>> emptyFuture(int timeout) {
    return CompletableFuture.supplyAsync(() -> {
      var message = utils.packageMessage(null, UUID.randomUUID().toString());
      try {
        Thread.sleep(timeout * 100);
      } catch (InterruptedException e) {}
      return message;
    });
  }
  
  private CompletableFuture<Message<MessagePayload>> exceptionalFuture() {
    return CompletableFuture.supplyAsync(() -> {
      throw new RuntimeException();
    });
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
