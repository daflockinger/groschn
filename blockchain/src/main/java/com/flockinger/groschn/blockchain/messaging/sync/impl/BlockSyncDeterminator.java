package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.BlockSynchronizationException;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.DeprecatedBlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.MerkleRootCalculator;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;

//FIXME DEPRECATED, TO BE REMOVED!
@Service
public class BlockSyncDeterminator implements SyncDeterminator {

  @Override
  public void determineAndSync() {
    // TODO Auto-generated method stub
    
  }
  // FIXME redo that service, make it simpler, make it work!!

 /* @Autowired
  private SyncKeeper synchronizer;
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private SyncInquirer inquirer;
  @Autowired
  private MerkleRootCalculator merkleCalculator;

  private final static int BATCH_SIZE = 100;
  private final static Logger LOG = LoggerFactory.getLogger(BlockSyncDeterminator.class);

  private final SyncBatchRequest request = SyncBatchRequest.build().batchSize(BATCH_SIZE)
      .idealReceiveNodeCount(11).maxFetchRetries(3).topic(MainTopics.BLOCK_INFO);

  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
  @Override
  public void determineAndSync() {
    var latestBlock = blockService.getLatestBlock();
    DeprecatedBlockInfoResult infoResult = new DeprecatedBlockInfoResult();

    for (int determinationRetry = 0; determinationRetry < 3
        && infoResult.getStartPosition() < 1; determinationRetry++) {
      infoResult = determineStartPositionAndInfos(latestBlock.getPosition(), infoResult);
    }
    if (infoResult.getStartPosition() > 0) {
      LOG.info("Sync point found at position: {}", infoResult.getStartPosition());
      blockService.removeBlocks(infoResult.getStartPosition());
      LOG.info("Successfully removed wrong blocks.");
      synchronizer.synchronize(infoResult);
      LOG.info("Successfully finished synchronization!");
    } else {
      throw new BlockSynchronizationException("Unable to Synchronize blocks with other nodes, "
          + "the majority of them seem to be very corrupt, or I'm the most up to date node!");
    }
  }

  private DeprecatedBlockInfoResult determineStartPositionAndInfos(long fromScanPosition,
      DeprecatedBlockInfoResult infoResult) {
    var blockInfos = getSortedBlockInfosFrom(fromScanPosition);
    var blocks = blockService.findBlocks(fromScanPosition, BATCH_SIZE);
    Collections.sort(blocks, Comparator.comparingLong(Block::getPosition).reversed());
    for (int blockCount = 0; blockCount < blocks.size()
        && blocks.size() <= blockInfos.size(); blockCount++) {
      if (isBlockHashCorrect(blocks.get(blockCount), blockInfos)) {
        infoResult.setStartPosition(blocks.get(blockCount).getPosition() + 1);
        infoResult.getCorrectInfos().addAll(blockInfos.subList(0, blockCount));
        break;
      }
    }
    if (infoResult.getStartPosition() == 0 && fromScanPosition > 1) {
      infoResult.getCorrectInfos().addAll(blockInfos);
      infoResult =
          determineStartPositionAndInfos(Math.max(1, fromScanPosition - BATCH_SIZE), infoResult);
    }
    if (infoResult.getCorrectInfos().isEmpty()) {
      infoResult.getCorrectInfos().addAll(blockInfos);
    }
    return infoResult;
  }

  private boolean isBlockHashCorrect(Block block, List<BlockInfo> blockInfos) {
    return StringUtils.equals(block.getHash(),
        blockInfos.stream().filter(info -> info.getPosition() != null)
            .filter(info -> info.getPosition() == block.getPosition()).findFirst()
            .orElse(new BlockInfo()).getBlockHash());
  }

  private List<BlockInfo> getSortedBlockInfosFrom(long fromPosition) {
    var infoResponse = inquirer
        .fetchNextBatch(SyncBatchRequest.build(request).fromPosition(fromPosition), BlockInfo.class)
        .stream().collect(groupingBy(this::merkleRootOfPayloadHashes, toList()))
        .entrySet().stream().map(Entry::getValue).reduce(this::findMajorlyAcceptedBatch).stream()
        .flatMap(Collection::stream).findFirst();
    return infoResponse.stream().map(SyncResponse::getEntities).flatMap(Collection::stream)
        .sorted(Comparator.reverseOrder()).collect(toList());
  }

  private <T extends Hashable<T>> String merkleRootOfPayloadHashes(SyncResponse<T> response) {
    return merkleCalculator.calculateMerkleRootHash(response.getEntities());
  }

  private <T extends Hashable<T>> List<SyncResponse<T>> findMajorlyAcceptedBatch(
      List<SyncResponse<T>> first, List<SyncResponse<T>> second) {
    return (first.size() > second.size()) ? first : second;
  }*/
}
