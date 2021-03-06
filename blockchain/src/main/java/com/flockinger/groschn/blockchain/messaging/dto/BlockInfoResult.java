package com.flockinger.groschn.blockchain.messaging.dto;

import java.util.List;

public class BlockInfoResult {
  private List<String> nodeIds;
  private List<BlockInfo> blockInfos;
  
  public BlockInfoResult(List<String> nodeIds, List<BlockInfo> blockInfos) {
    super();
    this.nodeIds = nodeIds;
    this.blockInfos = blockInfos;
  }
  
  public List<String> getNodeIds() {
    return nodeIds;
  }
  public List<BlockInfo> getBlockInfos() {
    return blockInfos;
  }
}
