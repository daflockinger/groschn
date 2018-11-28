package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.RESTART;
import static com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand.STOP;

import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.SmartBlockSynchronizer;
import com.flockinger.groschn.blockchain.messaging.sync.strategy.ScanningSyncStrategy;
import com.flockinger.groschn.commons.exception.BlockchainException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmartBlockSynchronizerImpl implements SmartBlockSynchronizer {

  @Autowired
  private BlockSynchronizer blockSynchronizer;
  @Autowired
  private ScanningSyncStrategy scanningSyncStrategy;
  @Autowired
  private BlockMaker blockMaker;
  
  private volatile SyncStatus syncStatus = SyncStatus.DONE;
  private final static Logger LOG = LoggerFactory.getLogger(SmartBlockSynchronizerImpl.class);

  @Override
  public void sync(Long fromPosition) {
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
      doSynchronize(fromPosition);
    } catch (BlockchainException e) {
      LOG.warn("Overall Block-Synchronization failed!", e);
    } finally {
      syncStatus = SyncStatus.DONE;
      LOG.info("Overall Block-Synchronization finished");
      blockMaker.generation(RESTART);
    }
  }

  private void doSynchronize(Long fromPosition) {
    var infoResult = scanningSyncStrategy.apply(fromPosition);

    if (infoResult.isPresent()) {
      var uniqueBlockInfos = new ArrayList<>(new HashSet<>(ListUtils.emptyIfNull(infoResult.get().getBlockInfos())));
      Collections.sort(uniqueBlockInfos);
      blockSynchronizer.synchronize(new BlockInfoResult(infoResult.get().getNodeIds(), uniqueBlockInfos));
    } else {
      LOG.warn("Synchronization request returned empty. Either no other nodes are connected or synchronization failed!");
    }
  }
}
