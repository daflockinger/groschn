package com.flockinger.groschn.messaging.members.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.FullNode;
import io.atomix.cluster.ClusterMembershipService;
import io.atomix.cluster.Member;
import io.atomix.cluster.MemberId;
import io.atomix.core.Atomix;

@Component
public class NetworkStatisticsImpl implements NetworkStatistics {

  @Autowired
  private Atomix atomix;

  @Override
  public long activeNodeCount() {
    return Long.valueOf(memberService().getReachableMembers().size());
  }

  @Override
  public List<String> activeNodeIds() {
    return memberService().getReachableMembers().stream()
        .filter(this::filterOutGateways)
        .map(Member::id)
        .map(MemberId::id).collect(Collectors.toList());
  }
  
  private ClusterMembershipService memberService() {
    return atomix.getMembershipService();
  }

  @Override
  public List<FullNode> activeFullNodes() {
    return memberService().getReachableMembers().stream()
        .filter(this::filterOutGateways)
        .map(this::mapToFullNode)
        .collect(Collectors.toList());
  }
  
  private FullNode mapToFullNode(Member member) {
    FullNode node = new FullNode();
    node.setName(member.id().id());
    node.setHost(member.address().host());
    node.setPort(member.address().port());
    return node;
  }
  
  private boolean filterOutGateways(Member member) {
    return !StringUtils.containsIgnoreCase(member.id().id(), GATEWAY_NODE_PREFIX);
  }
}
