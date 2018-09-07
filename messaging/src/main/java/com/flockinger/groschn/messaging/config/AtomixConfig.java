package com.flockinger.groschn.messaging.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.yml")
@ConfigurationProperties(prefix="atomix")
public class AtomixConfig {
  
  public final static String MANAGEMENT_PARTITION_GROUP_NAME = "management-group";
  
  private String hostNodeAddress;
  private Discovery discovery;
  private PartitionGroup partitionGroup;
  
  
  public String getHostNodeAddress() {
    return hostNodeAddress;
  }

  public void setHostNodeAddress(String hostNodeAddress) {
    this.hostNodeAddress = hostNodeAddress;
  }

  public Discovery getDiscovery() {
    return discovery;
  }

  public void setDiscovery(Discovery discovery) {
    this.discovery = discovery;
  }

  public PartitionGroup getPartitionGroup() {
    return partitionGroup;
  }

  public void setPartitionGroup(PartitionGroup partitionGroup) {
    this.partitionGroup = partitionGroup;
  }
  
  
  
  
  
  public static class PartitionGroup {
    private String name;
    private Integer numberPartitions;
    
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public Integer getNumberPartitions() {
      return numberPartitions;
    }
    public void setNumberPartitions(Integer numberPartitions) {
      this.numberPartitions = numberPartitions;
    }
  }
  
  
  
  public static class Discovery {
    private Long heartbeatMilliseconds;
    private Long failureTimeoutMilliseconds;
    private List<AtomixNode> bootstrapNodes;
    
    public Long getHeartbeatMilliseconds() {
      return heartbeatMilliseconds;
    }
    public void setHeartbeatMilliseconds(Long heartbeatMilliseconds) {
      this.heartbeatMilliseconds = heartbeatMilliseconds;
    }
    public Long getFailureTimeoutMilliseconds() {
      return failureTimeoutMilliseconds;
    }
    public void setFailureTimeoutMilliseconds(Long failureTimeoutMilliseconds) {
      this.failureTimeoutMilliseconds = failureTimeoutMilliseconds;
    }
    public List<AtomixNode> getBootstrapNodes() {
      return bootstrapNodes;
    }
    public void setBootstrapNodes(List<AtomixNode> bootstrapNodes) {
      this.bootstrapNodes = bootstrapNodes;
    }
  }
  
  public static class AtomixNode {
    private String name;
    private String address;
    
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
    public String getAddress() {
      return address;
    }
    public void setAddress(String address) {
      this.address = address;
    }
  }
}
