package com.flockinger.groschn.messaging.members;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.messaging.BaseAtomixTest;
import com.flockinger.groschn.messaging.members.impl.NetworkStatisticsImpl;

@ActiveProfiles("test1")
@ContextConfiguration(classes = {NetworkStatisticsImpl.class})
public class NetworkStatisticsIntegrationTest extends BaseAtomixTest {

  @Autowired
  private NetworkStatistics stats;
 
  @Test
  public void testActiveNodeCount_withRunningAtomix_shouldReturnCorrect() {
    long nodeCount = stats.activeNodeCount();
    
    assertEquals("verify correct node count", 1l, nodeCount);
  }
  
  @Test
  public void testActiveNodeIds_withRunningAtomix_shouldReturnCorrect() {
    var ids = stats.activeNodeIds();
    assertNotNull("verify that id's are not null", ids);
    assertFalse("verify that id's are not empty", ids.isEmpty());
    assertEquals("verify correct node name", "groschn2", ids.get(0));
  }
}
