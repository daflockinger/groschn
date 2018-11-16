package com.flockinger.groschn.messaging.config;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.flockinger.groschn.messaging.config.AtomixConfig.AtomixNode;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.cluster.messaging.ClusterCommunicationService;
import io.atomix.core.Atomix;
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;

@Configuration
public class MessagingProtocolConfiguration {
  
  @Autowired
  private AtomixConfig config;
  
  @Bean
  public Atomix createAndStartAtomixInstance() throws Exception {
    Atomix atomix = Atomix.builder()
        .withAddress(config.getHostNodeAddress())
        .withMemberId(MemberId.from(config.getNodeId()))
        .withMembershipProvider(BootstrapDiscoveryProvider.builder()
            .withNodes(mapNodes(config
                .getDiscovery().getBootstrapNodes(), this::mapToNode))
            .withHeartbeatInterval(Duration.ofMillis(config
                .getDiscovery().getHeartbeatMilliseconds()))
            .withFailureTimeout(Duration.ofMillis(config
                .getDiscovery().getFailureTimeoutMilliseconds()))
            .build())
        .withManagementGroup(RaftPartitionGroup.builder(config.getManagementGroup().getName())
            .withMembers(mapNodes(
                config.getDiscovery().getBootstrapNodes()
                ,this::mapToMember))
            .withNumPartitions(config.getManagementGroup().getNumberPartitions())
            /* should be sufficient to only store stuff in memory since it should be
             *  resynced anyways on a restart.
             */
            .withStorageLevel(config.getManagementGroup().getStorageLevel())
            .withDataDirectory(new File(config.getManagementGroup().getDataDirectory()))
            .build())
        /* Chosen cause of the faster propagation speed in comparison to Raft
         * which for this protocol is only used for leader election.
         * */
        .withPartitionGroups(PrimaryBackupPartitionGroup.builder(config.getPartitionGroup().getName())
            .withNumPartitions(config.getPartitionGroup().getNumberPartitions())
            .build())
        .withShutdownHookEnabled()
        .build();
    atomix.start().join();
    
    return atomix;
  }
    
  private <R> List<R> mapNodes(List<AtomixNode> atomixNodes, Function<AtomixNode, R> mapper) {
    return atomixNodes.stream()
        .filter(this::isNodeEntryNotEmpty)
        .map(mapper)
        .collect(Collectors.toList());
  }
  
  private boolean isNodeEntryNotEmpty(AtomixNode atomixNode) {
    return StringUtils.isNoneEmpty(atomixNode.getAddress(),atomixNode.getName());
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
  
  @Bean
  public ClusterCommunicationService clusterCommunicator() throws Exception {
    return createAndStartAtomixInstance().getCommunicationService();
  }
}
