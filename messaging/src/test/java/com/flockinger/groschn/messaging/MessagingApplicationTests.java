package com.flockinger.groschn.messaging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.messaging.inbound.MessageDispatcherTest.DonaldDuckListener;
import com.flockinger.groschn.messaging.outbound.BroadcasterIntegrationTest.GoofyResponder;

@ActiveProfiles("test3")
@RunWith(SpringRunner.class)
@SpringBootTest
@Import({GoofyResponder.class, DonaldDuckListener.class})
public class MessagingApplicationTests {
  
  @Test
  public void contextLoads() {}
}
