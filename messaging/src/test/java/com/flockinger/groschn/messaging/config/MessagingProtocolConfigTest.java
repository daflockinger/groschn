package com.flockinger.groschn.messaging.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.messaging.config.AtomixConfig.AtomixNode;
import com.flockinger.groschn.messaging.config.AtomixConfig.PartitionGroup;
import io.atomix.core.Atomix;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(AtomixConfig.class)
@ContextConfiguration(initializers=ConfigFileApplicationContextInitializer.class, classes = {MessagingProtocolConfig.class})
public class MessagingProtocolConfigTest {

  @Autowired
  private AtomixConfig config;
  @Autowired
  private MessagingProtocolConfig protocolConfig;
  
  @Test
  public void testConfig_shouldBeCorrect() {
    assertNotNull("verify atomix config is not null", config);
    assertNotNull("verify config node address is not null", config.getHostNodeAddress());
    assertNotNull("verify config discovery failure timeout is not null", 
        config.getDiscovery().getFailureTimeoutMilliseconds());
    assertNotNull("verify config discovery heartbeat is not null", 
        config.getDiscovery().getHeartbeatMilliseconds());
    assertNotNull("verify config discovery nodes are not null", 
        config.getDiscovery().getBootstrapNodes());
    List<AtomixNode> nodes = config.getDiscovery().getBootstrapNodes();
    assertFalse("verify that nodes contain something", nodes.isEmpty());
    assertTrue("verify nodes have all some contents", nodes.stream().allMatch(node -> 
      StringUtils.isNotEmpty(node.getAddress()) && StringUtils.isNotEmpty(node.getName())));
    PartitionGroup group = config.getPartitionGroup();
    assertNotNull("verify partitiongroup is not null", group);
    assertNotNull("verify partition number is not null", group.getNumberPartitions());
    assertNotNull("verify partition name is not null", group.getNumberPartitions());
  }
  
  
  @Test
  public void testProtocolConfig_shouldSomehowWork() {
    Atomix atomix = protocolConfig.createAtomixInstance();
    assertNotNull("verify created atomix instance is not null", atomix);
  }
  
}
