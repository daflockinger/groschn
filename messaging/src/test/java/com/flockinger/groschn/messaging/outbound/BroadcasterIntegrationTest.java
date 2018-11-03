package com.flockinger.groschn.messaging.outbound;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import java.util.Map;
import java.util.function.BiConsumer;
import org.junit.After;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.messaging.BaseAtomixTest;
import com.flockinger.groschn.messaging.ExecutorConfig;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.AbstractMessageResponder;
import com.flockinger.groschn.messaging.inbound.MessageDispatcherTest.DonaldDuckListener;
import com.flockinger.groschn.messaging.inbound.MessageDispatcherTest.PlutoListener;
import com.flockinger.groschn.messaging.inbound.MessageResponder;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.outbound.impl.BroadcasterImpl;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import com.github.benmanes.caffeine.cache.Cache;
import io.atomix.core.Atomix;

@ActiveProfiles("test2")
@ContextConfiguration(classes = {BroadcasterImpl.class, DonaldDuckListener.class, PlutoListener.class})
@Import(ExecutorConfig.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class})
public class BroadcasterIntegrationTest extends BaseAtomixTest {

  @Autowired
  private Atomix atomix;
  @MockBean
  private BlockSerializer serializer;
  @MockBean
  private NetworkStatistics fakeWorkStatistics;
  @MockBean
  private MessagingUtils utilsMock;
  
  @Test
  public void testInit_shouldHaveCalledCorrectly() {    
    var handlers = (Map<String, BiConsumer>) Whitebox.getInternalState(atomix.getMessagingService(), "handlers");
    var mickeyMouseHandler = handlers.get(MainTopics.SYNC_BLOCKCHAIN.name());
    var goofyHandler = handlers.get(MainTopics.SYNC_TRANSACTIONS.name());
    
    assertNull("verify totally weird stuff doesn't exist", handlers.get("BlackPete"));
    assertNotNull("verify first responder was registered", mickeyMouseHandler);
    assertNotNull("verify second responder was registered", goofyHandler);
  }
  
  @After
  public void teardown() {
    atomix.stop().join();
  }

  @Component
  public final static class MickeyMouseResponder extends AbstractMessageResponder<String> implements MessageResponder<MessagePayload> {
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.SYNC_BLOCKCHAIN;
    }

    @Override
    protected String getNodeId() {
      return "";
    }
    @Override
    protected Cache<String, String> getCache() {
      return mock(Cache.class);
    }
    @Override
    protected SyncResponse<String> createResponse(SyncRequest request) {
      return new SyncResponse<>();
    }
  }
  @Component
  public final static class GoofyResponder extends AbstractMessageResponder<String> implements MessageResponder<MessagePayload> {
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.SYNC_TRANSACTIONS;
    }
    
    @Override
    protected String getNodeId() {
      return "";
    }
    @Override
    protected Cache<String, String> getCache() {
      return mock(Cache.class);
    }
    @Override
    protected SyncResponse<String> createResponse(SyncRequest request) {
      return new SyncResponse<>();
    }
  }
}
