package com.flockinger.groschn.messaging.members;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.messaging.members.impl.NetworkStatisticsImpl;
import com.flockinger.groschn.messaging.model.FullNode;
import com.google.common.collect.ImmutableSet;
import io.atomix.cluster.ClusterMembershipService;
import io.atomix.cluster.Member;
import io.atomix.core.Atomix;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {NetworkStatisticsImpl.class})
public class NetworkStatisticsTest {

  @Autowired
  private NetworkStatistics statistics;
  
  @MockBean
  private Atomix atomix;
  @Mock
  private ClusterMembershipService membershipMock;
  
  @Before
  public void setup() {
    when(atomix.getMembershipService()).thenReturn(membershipMock);
  }
  
  @Test
  public void testActiveNodeCount_withRunningAtomix_shouldReturnCorrect() {
    when(membershipMock.getReachableMembers()).thenReturn(fakeMembers());
    
    long nodeCount = statistics.activeNodeCount();
    
    assertEquals("verify correct node count", 2l, nodeCount);
  }
  
  @Test
  public void testActiveNodeIds_withRunningAtomix_shouldReturnCorrect() {
    when(membershipMock.getReachableMembers()).thenReturn(fakeMembers());
    
    var ids = statistics.activeNodeIds();
    assertNotNull("verify that id's are not null", ids);
    assertEquals("verify correct node count", 2, ids.size());
    assertEquals("verify correct node name", "groschn01", ids.get(0));
  }
  
  @Test
  public void testActiveFullNodes_withRunningAtomix_shouldReturnCorrect() {
    when(membershipMock.getReachableMembers()).thenReturn(fakeMembers());
    
    List<FullNode> nodes = statistics.activeFullNodes();
    
    assertNotNull("verify returned nodes are not null", nodes);
    assertEquals("verify correct node count", 2, nodes.size());
    assertEquals("verify first node name", "groschn01", nodes.get(0).getName());
    assertEquals("verify first node host", "128.0.0.2", nodes.get(0).getHost());
    assertEquals("verify first node host", 1234, nodes.get(0).getPort().intValue());
  }
  
  private Set<Member> fakeMembers() {
    return ImmutableSet.of(Member.builder("groschn01").withAddress("128.0.0.2", 1234).build(), 
        Member.builder("groschn02").withAddress("129.0.0.3", 1234).build(),
        Member.builder("groschn-gateway-2").withAddress("127.0.0.1", 8080).build());
  }
}
