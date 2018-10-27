package com.flockinger.groschn.messaging.members.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
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
        .map(Member::id)
        .map(MemberId::id).collect(Collectors.toList());
  }
  
  private ClusterMembershipService memberService() {
    return atomix.getMembershipService();
  }
}
