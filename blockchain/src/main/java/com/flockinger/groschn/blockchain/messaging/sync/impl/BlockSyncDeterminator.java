package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockSynchronizationException;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncResponse;

@Service
public class BlockSyncDeterminator implements SyncDeterminator {
  
  @Autowired
  private SyncKeeper synchronizer;
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private SyncInquirer inquirer;
  
  private final static int BATCH_SIZE = 100;
  
  private final SyncBatchRequest request = SyncBatchRequest.build()
      .batchSize(BATCH_SIZE)
      .idealReceiveNodeCount(11)
      .maxFetchRetries(5)
      .topic(MainTopics.BLOCK_INFO);
  
  @Override
  public void determineAndSync() {
    var latestBlock = blockService.getLatestBlock();
    BlockInfoResult infoResult = new BlockInfoResult();
    
    for(int determinationRetry = 0; determinationRetry < 3 && infoResult.getStartPosition() < 1; determinationRetry++) {
      infoResult = determineStartPositionAndInfos(latestBlock.getPosition(), infoResult);
    }
    if(infoResult.getStartPosition() > 0) {
      blockService.removeBlocks(infoResult.getStartPosition());
      synchronizer.synchronize(infoResult);
    } else {
      throw new BlockSynchronizationException("Unable to Synchronize blocks with other nodes, "
          + "the majority of them seem to be very corrupt!");
    }
  }
  
  private BlockInfoResult determineStartPositionAndInfos(long fromScanPosition, BlockInfoResult infoResult) {   
    var blockInfos = getSortedBlockInfosFrom(fromScanPosition);   
    var blocks = blockService.findBlocks(fromScanPosition, BATCH_SIZE);
    Collections.sort(blocks, Comparator.comparingLong(Block::getPosition).reversed());
    for(int blockCount = 0; blockCount < blocks.size(); blockCount++) {
      if(isBlockHashCorrect(blocks.get(blockCount), blockInfos)) {
        infoResult.setStartPosition(blocks.get(blockCount).getPosition() + 1);
        infoResult.getCorrectInfos().addAll(blockInfos.subList(0, blockCount));
        break;
      }
    }
    if(infoResult.getStartPosition() == 0 && fromScanPosition > 1) {
      infoResult.getCorrectInfos().addAll(blockInfos);
      infoResult = determineStartPositionAndInfos(Math.max(1, fromScanPosition - BATCH_SIZE), infoResult);
    }
    if(infoResult.getCorrectInfos().isEmpty()) {
      infoResult.getCorrectInfos().addAll(blockInfos);
    }
    return infoResult;
  }
  
  private boolean isBlockHashCorrect(Block block, List<BlockInfo> blockInfos) {
    return StringUtils.equals(block.getHash(), blockInfos.stream()
        .filter(info -> info.getPosition() != null)
        .filter(info -> info.getPosition() == block.getPosition()).findFirst()
        .orElse(new BlockInfo()).getBlockHash() );
  }
  
  private List<BlockInfo> getSortedBlockInfosFrom(long fromPosition) {
    var infoResponse = inquirer.fetchNextBatch(SyncBatchRequest
        .build(request).fromPosition(fromPosition), BlockInfo.class);
    return infoResponse.stream()
        .map(SyncResponse::getEntities)
        .flatMap(Collection::stream)
        .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
  }
}