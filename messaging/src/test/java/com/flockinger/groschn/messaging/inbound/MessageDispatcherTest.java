package com.flockinger.groschn.messaging.inbound;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import com.flockinger.groschn.messaging.inbound.MessageDispatcherTest.DonaldDuckListener;
import com.flockinger.groschn.messaging.inbound.MessageDispatcherTest.PlutoListener;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import io.atomix.core.Atomix;

@ActiveProfiles("test2")
@ContextConfiguration(classes = {MessageDispatcher.class, 
    DonaldDuckListener.class, PlutoListener.class})
@Import(ExecutorConfig.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class})
public class MessageDispatcherTest extends BaseAtomixTest {
  
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
    var plutoHandler = handlers.get(MainTopics.FRESH_TRANSACTION.name());
    var donaldDuckHandler = handlers.get(MainTopics.FRESH_BLOCK.name());
    
    assertNull("verify totally weird stuff doesn't exist", handlers.get("BlackPete"));
    assertNotNull("verify first listener was registered", plutoHandler);
    assertNotNull("verify second listener was registered", donaldDuckHandler);
  }
  
  @After
  public void teardown() {
    atomix.stop().join();
  }
  
  @Component
  public final static class PlutoListener implements MessageListener<MessagePayload> {
    @Override
    public void receiveMessage(Message<MessagePayload> message) {      
    }
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.FRESH_TRANSACTION;
    }
  }
  @Component
  public final static class DonaldDuckListener implements MessageListener<MessagePayload> {
    @Override
    public void receiveMessage(Message<MessagePayload> message) {      
    }
    @Override
    public MainTopics getSubscribedTopic() {
      return MainTopics.FRESH_BLOCK;
    }
  }
}
