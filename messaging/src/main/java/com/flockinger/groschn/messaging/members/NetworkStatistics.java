package com.flockinger.groschn.messaging.members;

import java.util.List;
import com.flockinger.groschn.messaging.model.FullNode;

public interface NetworkStatistics {
  
  static final String GATEWAY_NODE_PREFIX = "gateway";
  
  long activeNodeCount();
  
  List<String> activeNodeIds();
  
  List<FullNode> activeFullNodes();
}
