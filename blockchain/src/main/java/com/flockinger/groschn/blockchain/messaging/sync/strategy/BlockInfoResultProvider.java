package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResponse;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockChainSelector;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;

@Component
class BlockInfoResultProvider {
  
  @Autowired
  private BlockChainSelector chainSelector;
  @Autowired
  private SyncInquirer inquirer;
  @Autowired
  private BlockStorageService blockService;
  
  private final SyncBatchRequest request = SyncBatchRequest.build()
      .idealReceiveNodeCount(50)
      .maxFetchRetries(3)
      .topic(MainTopics.BLOCK_INFO);
  
  public Optional<BlockInfoResult> fetchBlockInfos(long fromPosition, int batchSize) {
    fromPosition = Math.max(1, fromPosition);
    batchSize = Math.max(1, batchSize);
    var syncResponses = inquirer.fetchNextBatch(request.fromPosition(fromPosition).batchSize(batchSize), BlockInfo.class);
    
    syncResponses.add(getOwnBlockInfos(fromPosition, batchSize));
    
    return chainSelector.choose(syncResponses.stream()
                            .map(this::mapToInfoResponse)
                            .collect(Collectors.toList()));
  }
  
  private SyncResponse<BlockInfo> getOwnBlockInfos(long fromPosition, int batchSize) {
    var ownSyncResponse = new SyncResponse<BlockInfo>();
    ownSyncResponse.setStartingPosition(fromPosition);
    ownSyncResponse.setEntities(blockService.findBlocks(fromPosition, batchSize)
        .stream().map(this::mapToBlockInfo).collect(Collectors.toList()));
    return ownSyncResponse;
  }
  
  private BlockInfo mapToBlockInfo(Block block) {
    var blockInfo = new BlockInfo();
    blockInfo.setBlockHash(block.getHash());
    blockInfo.setPosition(block.getPosition());
    return blockInfo;
  }
  
  private BlockInfoResponse mapToInfoResponse(SyncResponse<BlockInfo> syncResponse) {    
    return new BlockInfoResponse(syncResponse.getNodeId(), syncResponse.getEntities());
  }
}
