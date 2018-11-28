package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.sync.BlockSyncStrategy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScanningSyncStrategy implements BlockSyncStrategy{

  @Autowired
  private BlockInfoResultProvider infoResultProvider;
  @Autowired
  private ScanResultMatcher resultMatcher;
  
  private final static int BATCH_SIZE = 10;
  
  @Override
  public Optional<BlockInfoResult> apply(Long fromPosition) {
    var adjustedFromPosition = fromPosition;
    var currentInfoResult = infoResultProvider.fetchBlockInfos(adjustedFromPosition, BATCH_SIZE);
    var infoResults = new ArrayList<BlockInfoResult>();
    
    findSyncStartGatheringInfoResults(ScanningContext.build(infoResults)
                                                    .fromPosition(adjustedFromPosition)
                                                    .batchSize(BATCH_SIZE)
                                                    .currentResult(currentInfoResult));
    var nodeIds = infoResults.stream().map(BlockInfoResult::getNodeIds).flatMap(Collection::stream)
                      .collect(Collectors.toSet()).stream()
                      .collect(Collectors.toList());
    var blockInfos = infoResults.stream().map(BlockInfoResult::getBlockInfos).flatMap(Collection::stream)
                      .sorted().collect(Collectors.toList());
    return Optional.of(new BlockInfoResult(nodeIds, blockInfos));
  }
  
  private void findSyncStartGatheringInfoResults(ScanningContext context) {
    var probeResult = resultMatcher.findMatchingStartPosition(context);
    context.addFinalResult(adjustedResult(context.current(), probeResult));
    
    if(!probeResult.isPresent() && context.getFromPosition() > 1){
      adjustBatchSize(context);
      adjustFromPosition(context);
      context.currentResult(infoResultProvider.fetchBlockInfos(context.getFromPosition(), context.getBatchSize()));
      findSyncStartGatheringInfoResults(context);
    } 
  }
  
  private Optional<BlockInfoResult> adjustedResult(Optional<BlockInfoResult> currentResult, Optional<Long> probeResult) {
    if(currentResult.isPresent() && probeResult.isPresent()) {
      Collections.sort(currentResult.get().getBlockInfos());
      var toSyncBlockInfos = currentResult.get().getBlockInfos().stream()
            .dropWhile(it ->  it.getPosition() < probeResult.get())
            .collect(Collectors.toList());
      return Optional.of(new BlockInfoResult(currentResult.get().getNodeIds(), toSyncBlockInfos));
    } else {
      return currentResult;
    }
  }
  
  private void adjustBatchSize(ScanningContext context) {
    if((context.getFromPosition() - BATCH_SIZE) < 1) {
      context.batchSize(context.getFromPosition() .intValue() - 1);
    }
  }
  
  private void adjustFromPosition(ScanningContext context) {
    context.fromPosition(Math.max(1, context.getFromPosition() - BATCH_SIZE));
  }
}
