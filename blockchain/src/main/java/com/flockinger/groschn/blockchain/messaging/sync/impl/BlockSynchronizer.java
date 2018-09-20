package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.messaging.config.MainTopics;
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
  private final static Integer BLOCK_FETCH_N_STORE_RETRIES = 1;
  
  private final SyncBatchRequest batchRequest = SyncBatchRequest.build()
      .batchSize(BLOCK_REQUEST_PACKAGE_SIZE) 
      // Ideal amount of nodes that responded the synchronization request.
      .idealReceiveNodeCount(3)
      //Amount of retires to request ideally three (and with a small network one) synchronization responses.
      .maxFetchRetries(3)
      // Block sync request topic
      .topic(MainTopics.SYNC_BLOCKCHAIN);
  
  
  //TODO get rid of this wild WHILE loop
  @Override
  public void synchronize(long fromPosition) {
    if(syncStatus().equals(SyncStatus.IN_PROGRESS.name())) {
      LOG.warn("Synchronization still in progress!");
      return;
    }
    setSyncStatus(SyncStatus.IN_PROGRESS);
    LOG.debug("Started synchronization at position: " + fromPosition);
    try {
      boolean hasFinishedSync = false;
      while(!hasFinishedSync) {
        hasFinishedSync = storeBatchOfBlocksAndReturnHasFinished(SyncBatchRequest.build(batchRequest).fromPosition(fromPosition));
        LOG.debug("Successfully synced and stored Blocks until position: " + fromPosition);
        fromPosition += BLOCK_REQUEST_PACKAGE_SIZE;
      }
    } finally {
      setSyncStatus(SyncStatus.DONE);
      LOG.debug("Synchronization finished");
    }
  }
  
  private boolean storeBatchOfBlocksAndReturnHasFinished(SyncBatchRequest request) {
    Optional<SyncResponse<Block>> blocks = Optional.empty();
    boolean isValidAndStored = false;
    for(int retryCount = 0; retryCount <= BLOCK_FETCH_N_STORE_RETRIES && !isValidAndStored; retryCount++) {
      blocks = inquirer.fetchNextBatch(request, Block.class);
      isValidAndStored = blocks.stream().map(SyncResponse::getEntities)
          .filter(Objects::nonNull)
          .map(this::storeBlocksAndReturnSuccess)
          .findFirst().orElse(true);
    }
    return blocks.stream().map(response -> 
    response.isLastPositionReached() || emptyIfNull(response.getEntities()).isEmpty())
        .findFirst().orElse(true) || !isValidAndStored;
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
