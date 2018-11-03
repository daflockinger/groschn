package com.flockinger.groschn.messaging.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.flockinger.groschn.messaging.BaseAtomixTest;
import com.flockinger.groschn.messaging.config.AtomixConfig.AtomixNode;
import com.flockinger.groschn.messaging.config.AtomixConfig.ManagementGroup;
import com.flockinger.groschn.messaging.config.AtomixConfig.PartitionGroup;
import io.atomix.core.Atomix;

public class MessagingProtocolConfigTest extends BaseAtomixTest {

  @Autowired
  private AtomixConfig config;
  @Autowired
  private MessagingProtocolConfig protocolConfig; 
  
  @Test
  public void testConfig_shouldBeCorrect() {
    assertNotNull("verify atomix config is not null", config);
    assertEquals("verify correct node id", "groschn1", config.getNodeId());
    assertNotNull("verify config node address is not null", config.getHostNodeAddress());
    assertNotNull("verify config discovery failure timeout is not null", 
        config.getDiscovery().getFailureTimeoutMilliseconds());
    assertNotNull("verify config discovery heartbeat is not null", 
        config.getDiscovery().getHeartbeatMilliseconds());
    assertNotNull("verify config discovery nodes are not null", 
        config.getDiscovery().getBootstrapNodes());
    List<AtomixNode> nodes = config.getDiscovery().getBootstrapNodes();
    assertFalse("verify that nodes contain something", nodes.isEmpty());
    PartitionGroup group = config.getPartitionGroup();
    assertNotNull("verify partitiongroup is not null", group);
    assertNotNull("verify partition number is not null", group.getNumberPartitions());
    assertNotNull("verify partition name is not null", group.getNumberPartitions());
    ManagementGroup management = config.getManagementGroup();
    assertNotNull("verify partitiongroup is not null", management);
    assertNotNull("verify partition number is not null", management.getNumberPartitions());
    assertNotNull("verify partition name is not null", management.getName());
    assertNotNull("verify partition data dir is not null", management.getDataDirectory());
    assertNotNull("verify partition storage level is not null", management.getStorageLevel());
  }
  
  @Test
  public void testProtocolConfig_shouldSomehowWork() throws Exception {
    Atomix atomix = protocolConfig.createAndStartAtomixInstance();
    assertNotNull("verify created atomix instance is not null", atomix);
    
    atomix.stop().join();
  }
}
