package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockSynchronizationException;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;

@Service
public class BlockSyncDeterminator implements SyncDeterminator {
  
  @Autowired
  private BlockSynchronizer synchronizer;
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
    long startPosition = 0l;
    
    for(int determinationRetry = 0; determinationRetry < 3 && startPosition < 1; determinationRetry++) {
      startPosition = determineStartPosition(latestBlock.getPosition(), startPosition);
    }
    if(startPosition > 0) {
      blockService.removeBlocks(startPosition);
      synchronizer.synchronize(startPosition);
    } else {
      throw new BlockSynchronizationException("Unable to Synchronize blocks with other nodes, "
          + "the majority of them seem to be very corrupt!");
    }
  }
  
  private long determineStartPosition(long fromScanPosition, long startPosition) {   
    var blockInfos = getSortedBlockInfosFrom(fromScanPosition);   
    var blocks = blockService.findBlocks(fromScanPosition, BATCH_SIZE);
    Collections.sort(blocks, Comparator.comparingLong(Block::getPosition).reversed());
    for(int blockCount = 0; blockCount < blocks.size(); blockCount++) {
      if(isBlockHashCorrect(blocks.get(blockCount), blockInfos)) {
        startPosition = blocks.get(blockCount).getPosition() + 1;
        break;
      }
    }
    if(startPosition == 0 && fromScanPosition > 1) {
      startPosition = determineStartPosition(Math.max(1, fromScanPosition - BATCH_SIZE), startPosition);
    }
    return startPosition;
  }
  
  private boolean isBlockHashCorrect(Block block, Map<Long, String> blockInfos) {
    return StringUtils.equals(block.getHash(), blockInfos.get(block.getPosition()));
  }
  
  private Map<Long, String> getSortedBlockInfosFrom(long fromPosition) {
    var infoResponse = inquirer.fetchNextBatch(SyncBatchRequest
        .build(request).fromPosition(fromPosition), BlockInfo.class);
    return infoResponse.stream()
        .map(SyncResponse::getEntities)
        .flatMap(Collection::stream)
        .sorted().collect(Collectors
            .toMap(BlockInfo::getPosition, BlockInfo::getBlockHash));
  }
}
