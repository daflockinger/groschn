package com.flockinger.groschn.messaging.members.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.messaging.members.ElectionStatistics;
import io.atomix.core.Atomix;

@Component
public class ElectionStatisticsImpl implements ElectionStatistics {

  @Autowired
  private Atomix atomix;
  
  @Override
  public Long currentActiveVoterCount() {
    return Long.valueOf(atomix.getMembershipService().getReachableMembers().size());
  }

}
