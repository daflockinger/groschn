package com.flockinger.groschn.messaging.config;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.flockinger.groschn.messaging.config.AtomixConfig.AtomixNode;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import io.atomix.storage.StorageLevel;

@Configuration
public class MessagingProtocolConfig {
  
  @Autowired
  private AtomixConfig config;
  
  @Bean
  public Atomix createAtomixInstance() {
    
    Atomix atomix = Atomix.builder()
        .withAddress(config.getHostNodeAddress())
        .withMembershipProvider(BootstrapDiscoveryProvider.builder()
            .withNodes(mapNodes(config
                .getDiscovery().getBootstrapNodes(), this::mapToNode))
            .withHeartbeatInterval(Duration.ofMillis(config
                .getDiscovery().getHeartbeatMilliseconds()))
            .withFailureTimeout(Duration.ofMillis(config
                .getDiscovery().getFailureTimeoutMilliseconds()))
            .build())
        .withManagementGroup(RaftPartitionGroup.builder(AtomixConfig.MANAGEMENT_PARTITION_GROUP_NAME)
            .withMembers(mapNodes(
                config.getDiscovery().getBootstrapNodes()
                ,this::mapToMember))
            .withNumPartitions(config.getPartitionGroup().getNumberPartitions())
            /* should be sufficient to only store stuff in memory since it should be
             *  resynced anyways on a restart.
             */
            .withStorageLevel(StorageLevel.MEMORY)
            .build())
        /* Chosen cause of the faster propagation speed in comparison to Raft
         * which for this protocol is only used for leader election.
         * */
        .withPartitionGroups(PrimaryBackupPartitionGroup.builder(config.getPartitionGroup().getName())
            .withNumPartitions(config.getPartitionGroup().getNumberPartitions())
            .build())
        .withShutdownHookEnabled()
        .build();
    return atomix;
  }
  
  
  private <R> List<R> mapNodes(List<AtomixNode> atomixNodes, Function<AtomixNode, R> mapper) {
    return atomixNodes.stream().map(mapper).collect(Collectors.toList());
  }
  
  private Node mapToNode(AtomixNode atomixNode) {
    return Node.builder()
        .withAddress(atomixNode.getAddress())
        .withId(atomixNode.getName())
        .build();
  }
  
  private String mapToMember(AtomixNode atomixNode) {
    return atomixNode.getName();
  }
}
