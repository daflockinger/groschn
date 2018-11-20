package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.RESTART;
import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.STOP;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStrategyType;
import com.flockinger.groschn.blockchain.messaging.sync.BlockSyncStrategy;
import com.flockinger.groschn.blockchain.messaging.sync.SmartBlockSynchronizer;
import com.flockinger.groschn.commons.exception.BlockchainException;

@Component
public class SmartBlockSynchronizerImpl implements SmartBlockSynchronizer {

  @Autowired
  private BlockSynchronizer blockSynchronizer;
  @Autowired
  private List<BlockSyncStrategy> strategies;
  @Autowired
  private BlockMaker blockMaker;
  
  private volatile SyncStatus syncStatus = SyncStatus.DONE;
  private final static Logger LOG = LoggerFactory.getLogger(SmartBlockSynchronizerImpl.class);
    
  @Override
  public void sync(SyncSettings settings) {
    synchronized (this) {
      if(syncStatus.equals(SyncStatus.IN_PROGRESS)) {
        LOG.warn("Overall Block-Synchronization still in progress!");
        return;
      } else {
        blockMaker.generation(STOP);
        syncStatus = SyncStatus.IN_PROGRESS;
      }
    }
    try {
      doSynchronize(settings);
    } catch (BlockchainException e) {
      LOG.warn("Overall Block-Synchronization failed!", e);
    } finally {
      syncStatus = SyncStatus.DONE;
      LOG.info("Overall Block-Synchronization finished");
      blockMaker.generation(RESTART);
    }
  }
  
  private void doSynchronize(SyncSettings settings) {
    var strategy = getStrategy(settings.getStrategyType());
    var infoResult = strategy.apply(settings);
    
    if(!infoResult.isPresent()) {
      infoResult = getStrategy(SyncStrategyType.FALLBACK).apply(settings);
    }
    if (infoResult.isPresent()) {
      blockSynchronizer.synchronize(infoResult.get());
    } else {
      LOG.warn("Synchronization request returned empty. Either no other nodes are connected or synchronization failed!");
    }
  }
  
  private BlockSyncStrategy getStrategy(SyncStrategyType type) {
    return strategies.stream().filter(strategy -> strategy.isApplicable(type)).findFirst().get();
  }
}
