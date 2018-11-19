package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import java.util.Collection;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStrategyType;
import com.flockinger.groschn.blockchain.messaging.sync.BlockSyncStrategy;

@Component
public class ConfidentSyncStrategy implements BlockSyncStrategy {

  @Autowired
  private BlockInfoResultProvider infoResultProvider;
  @Autowired
  private BlockStorageService blockService;
  
  @Override
  public Optional<BlockInfoResult> apply(SyncSettings settings) {
    final int batchSize = (int)(settings.getToPos() - settings.getFromPos());
    var infoResult = infoResultProvider.fetchBlockInfos(settings.getFromPos(), batchSize);
    
    if(!isOverlappingBlockInfoMatchingExistingBlock(infoResult, settings)) {
      return Optional.empty();
    }
    return infoResult;
  }
  
  private boolean isOverlappingBlockInfoMatchingExistingBlock(Optional<BlockInfoResult> infoResult, SyncSettings settings) {
    var firstBlockInfo = infoResult.stream()
        .map(BlockInfoResult::getBlockInfos)
        .flatMap(Collection::stream)
        .filter(it -> it.getPosition() == settings.getFromPos())
        .findFirst();
    var overlappingStoredBlock = firstBlockInfo.stream()
        .map(it -> blockService.findBlocks(it.getPosition(), 1))
        .flatMap(Collection::stream)
        .findFirst();
    if(overlappingStoredBlock.isPresent()) {
      return StringUtils.equalsIgnoreCase(overlappingStoredBlock.get().getHash(), firstBlockInfo.get().getBlockHash());
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isApplicable(SyncStrategyType startegy) {
    return SyncStrategyType.CONFIDENT.equals(startegy);
  }
}
