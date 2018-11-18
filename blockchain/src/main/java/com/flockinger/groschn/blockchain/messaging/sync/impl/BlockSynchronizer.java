package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.RESTART;
import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.STOP;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.collections4.ListUtils.partition;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockSynchronizationException;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.exception.BlockchainException;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.RequestHeader;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class BlockSynchronizer implements SyncKeeper {
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private BlockMaker blockMaker;
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
  
  
 private final static RetryTemplate retryTemplate = new RetryTemplate();
  
  public BlockSynchronizer() {
    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(BLOCK_FETCH_N_STORE_RETRIES);
    retryTemplate.setRetryPolicy(retryPolicy);
  }
  
  
  @Override
  public void synchronize(BlockInfoResult infoResult) { 
    synchronized (this) {
      if(syncStatus().equals(SyncStatus.IN_PROGRESS.name())) {
        LOG.warn("Synchronization still in progress!");
        return;
      } else {
        blockMaker.generation(STOP);
      }
      setSyncStatus(SyncStatus.IN_PROGRESS);
    }
    try {
      doSynchronize(infoResult);
    } catch (BlockchainException e) {
      LOG.warn("Synchronization failed!", e);
    } finally {
      setSyncStatus(SyncStatus.DONE);
      LOG.info("Synchronization finished");
      blockMaker.generation(RESTART);
    }
  }

  private void doSynchronize(BlockInfoResult infoResult) {
    var blockInfos = emptyIfNull(infoResult.getBlockInfos());
    var fromPosition = blockInfos.stream().map(BlockInfo::getPosition).reduce(Math::min).orElse(1l);
    Collections.sort(blockInfos);
    batchRequest.selectedNodeIds(infoResult.getNodeIds());

    for (var relatedInfos : partition(blockInfos, BLOCK_REQUEST_PACKAGE_SIZE)) {
      var request = SyncBatchRequest.build(batchRequest).fromPosition(fromPosition)
          .headers(mapToHeaders(relatedInfos));

      retryTemplate.execute(it -> storeBatch(request, relatedInfos));

      LOG.info("Successfully synced and stored Blocks until position: " + fromPosition);
      fromPosition += BLOCK_REQUEST_PACKAGE_SIZE;
    }
  }
    
  private List<RequestHeader> mapToHeaders(List<BlockInfo> infos) {
    return infos.stream().map(info -> {
      var header = new RequestHeader();
      header.setPosition(info.getPosition());
      header.setHash(info.getBlockHash());
      return header;
    }).collect(Collectors.toList());
  }
  
  private boolean storeBatch(SyncBatchRequest request, List<BlockInfo> relatedInfos) {
    LOG.info("fetching next block batch");
    var syncResponse = inquirer.fetchNextBatch(request, Block.class).stream()
        .filter(it -> isResponseCorrect(it, relatedInfos))
        .reduce(this::getBiggerBatch);
    
    if(!syncResponse.isPresent() || isEmpty(syncResponse.get().getEntities())) {
      throw new BlockSynchronizationException("No blocks received, retrying!");
    }
    LOG.info("done fetching next block batch");
    syncResponse.stream()
        .map(SyncResponse::getEntities)
        .flatMap(Collection::stream)
        .sorted()
        .forEach(blockService::saveInBlockchain);
    return true;
  }
  
  private <T extends Hashable<T>> SyncResponse<T> getBiggerBatch(SyncResponse<T> batchOne, SyncResponse<T> batchTwo) {
    return (batchOne.getEntities().size() >= batchTwo.getEntities().size()) ? batchOne : batchTwo;
  }
  
  private boolean isResponseCorrect(SyncResponse<Block> response, List<BlockInfo> relatedInfos) {
    var respondedBlockInfos = emptyIfNull(response.getEntities()).stream().filter(Objects::nonNull)
        .map(this::mapToInfo).collect(Collectors.toList());
    LOG.info("Did response validate correct: " + respondedBlockInfos.containsAll(relatedInfos));
    //Only works properly if the added Equals in BlockInfo works!
    return respondedBlockInfos.containsAll(relatedInfos);
  }
  
  private BlockInfo mapToInfo(Block block) {
    var info = new BlockInfo();
    info.setBlockHash(block.getHash());
    info.setPosition(block.getPosition());
    return info;
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
