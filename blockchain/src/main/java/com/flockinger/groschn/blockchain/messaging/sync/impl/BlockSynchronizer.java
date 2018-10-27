package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.commons.exception.BlockchainException;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class BlockSynchronizer implements SyncKeeper {
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  @Qualifier("SyncBlockId_Cache")
  private Cache<String, String> syncBlockIdCache;
  @Autowired
  private SyncInquirer inquirer;
  
  @Value("${atomix.node-id}")
  private String nodeId;
  
  private final static Logger LOG = LoggerFactory.getLogger(BlockSynchronizer.class);
 
  /**
   * 
   */
  private final static int BLOCK_REQUEST_PACKAGE_SIZE = 10;
  
  /**
   * Number of retries when the validation of one of the received Blocks fails.
   */
  private final static Integer BLOCK_FETCH_N_STORE_RETRIES = 20;
  
  private final SyncBatchRequest batchRequest = SyncBatchRequest.build()
      .batchSize(BLOCK_REQUEST_PACKAGE_SIZE) 
      // Ideal amount of nodes that responded the synchronization request.
      .idealReceiveNodeCount(1)
      //Amount of retires to request ideally five (and with a small network one) synchronization responses.
      .maxFetchRetries(5)
      // Block sync request topic
      .topic(MainTopics.SYNC_BLOCKCHAIN);
  
  @Override
  public void synchronize(BlockInfoResult infoResult) {
    var fromPosition = infoResult.getStartPosition();
    var blockInfos = ListUtils.emptyIfNull(infoResult.getCorrectInfos());
    var totalBatches = Math.round(Math.ceil(blockInfos.size()/BLOCK_REQUEST_PACKAGE_SIZE));
    Collections.sort(blockInfos);
    if(syncStatus().equals(SyncStatus.IN_PROGRESS.name())) {
      LOG.warn("Synchronization still in progress!");
      return;
    }
    setSyncStatus(SyncStatus.IN_PROGRESS);
    LOG.debug("Started synchronization at position: " + fromPosition);
    try {
      boolean hasFinishedSync = false;
      for(int batchCount =0;(batchCount < totalBatches) && !hasFinishedSync; batchCount++) {
        var relatedInfos = extractPartition(blockInfos, batchCount);
        hasFinishedSync = storeBatchOfBlocksAndReturnHasFinished(SyncBatchRequest
            .build(batchRequest).fromPosition(fromPosition), relatedInfos);
        LOG.debug("Successfully synced and stored Blocks until position: " + fromPosition);
        fromPosition += BLOCK_REQUEST_PACKAGE_SIZE;
      }
    } finally {
      setSyncStatus(SyncStatus.DONE);
      LOG.debug("Synchronization finished");
    }
  }
  
  private List<BlockInfo> extractPartition(List<BlockInfo> allInfos, int position) {
    var splitInfos = ListUtils.partition(allInfos, BLOCK_REQUEST_PACKAGE_SIZE);
    return position < splitInfos.size() ? splitInfos.get(position) : new ArrayList<>();
  }
  
  private boolean storeBatchOfBlocksAndReturnHasFinished(SyncBatchRequest request, List<BlockInfo> relatedInfos) {
    Optional<SyncResponse<Block>> syncResponse = Optional.empty();
    boolean isValidAndStored = false;
    for(int retryCount = 0; retryCount <= BLOCK_FETCH_N_STORE_RETRIES && !isValidAndStored; retryCount++) {
      syncResponse = inquirer.fetchNextBatch(request, Block.class);      
      if(isResponseCorrect(syncResponse, relatedInfos)) {
        isValidAndStored = syncResponse.stream().map(SyncResponse::getEntities)
            .filter(Objects::nonNull)
            .map(this::storeBlocksAndReturnSuccess)
            .findFirst().orElse(true);
      }
    }
    return syncResponse.stream().map(response -> 
    response.isLastPositionReached() || ListUtils.emptyIfNull(response.getEntities()).isEmpty())
        .findFirst().orElse(true) || !isValidAndStored;
  }
  
  private boolean isResponseCorrect(Optional<SyncResponse<Block>> response, List<BlockInfo> relatedInfos) {
    var respondedBlockInfos = response.stream()
        .map(SyncResponse::getEntities).filter(Objects::nonNull).flatMap(Collection::stream)
        .map(this::mapToInfo).collect(Collectors.toList());
    //Only works properly if the added Equals in BlockInfo works!
    return respondedBlockInfos.containsAll(relatedInfos);
  }
  
  private BlockInfo mapToInfo(Block block) {
    var info = new BlockInfo();
    info.setBlockHash(block.getHash());
    info.setPosition(block.getPosition());
    return info;
  }
  
  private boolean storeBlocksAndReturnSuccess(List<Block> blocks) {
    boolean allStoredSuccessful = false;
    Collections.sort(blocks, Comparator.comparing(Block::getPosition));
    try {
      for(Block block: blocks) {
        blockService.saveInBlockchain(block);
      }
      allStoredSuccessful = true;
    } catch(BlockchainException e) {
      LOG.error("Received invalid Block during Block-Synchronization!", e);
    }
    return allStoredSuccessful;
  }
  
  @Override
  public String syncStatus() {
    return StringUtils.defaultString(syncBlockIdCache
        .getIfPresent(SyncStatus.SYNC_STATUS_CACHE_KEY),SyncStatus.DONE.name());
  }
  private void setSyncStatus(SyncStatus status) {
    syncBlockIdCache.put(SyncStatus.SYNC_STATUS_CACHE_KEY, status.name());
  }
}