package com.flockinger.groschn.messaging.members;

import java.util.List;

public interface NetworkStatistics {
  
  long activeNodeCount();
  
  List<String> activeNodeIds();
}
