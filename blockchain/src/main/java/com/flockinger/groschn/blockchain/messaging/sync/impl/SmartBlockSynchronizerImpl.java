package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStrategyType;
import com.flockinger.groschn.blockchain.messaging.sync.BlockSyncStrategy;
import com.flockinger.groschn.blockchain.messaging.sync.SmartBlockSynchronizer;

@Component
public class SmartBlockSynchronizerImpl implements SmartBlockSynchronizer {

  @Autowired
  private BlockSynchronizer blockSynchronizer;
  @Autowired
  private List<BlockSyncStrategy> strategies;
    
  @Override
  public void sync(SyncSettings settings) {
    var strategy = getStrategy(settings.getStrategyType());
    var infoResult = strategy.apply(settings);
    
    if(!infoResult.isPresent()) {
      infoResult = getStrategy(SyncStrategyType.FALLBACK).apply(settings);
    }
    doSynchronize(infoResult);
  }
  
  private BlockSyncStrategy getStrategy(SyncStrategyType type) {
    return strategies.stream().filter(strategy -> strategy.isApplicable(type)).findFirst().get();
  }

  //TODO analyze kind of failure and react accordingly!
  private void doSynchronize(Optional<BlockInfoResult> infoResult) {
    if (infoResult.isPresent()) {
      blockSynchronizer.synchronize(infoResult.get());
    }
  }
}
