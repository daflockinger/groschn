package com.flockinger.groschn.blockchain.messaging.dto;

import java.util.List;

public class BlockInfoResponse {
  
  private String nodeId;
  private List<BlockInfo> blockInfos;
  
  public BlockInfoResponse(String nodeId, List<BlockInfo> blockInfos) {
    super();
    this.nodeId = nodeId;
    this.blockInfos = blockInfos;
  }
  
  public String getNodeId() {
    return nodeId;
  }
  public List<BlockInfo> getBlockInfos() {
    return blockInfos;
  }
}
