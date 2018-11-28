package com.flockinger.groschn.blockchain.messaging.sync;

import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SyncKeeper {

  private final GlobalBlockchainStatistics statistics;
  private final BlockStorageService blockService;
  private final SmartBlockSynchronizer synchronizer;

  private final static long MAX_BLOCKCHAIN_POSITION_GAP = 3L;
  private final static Logger LOG = LoggerFactory.getLogger(SyncKeeper.class);

  public SyncKeeper(
      GlobalBlockchainStatistics statistics,
      BlockStorageService blockService,
      SmartBlockSynchronizer synchronizer
  ) {
    this.statistics = statistics;
    this.blockService = blockService;
    this.synchronizer = synchronizer;
  }

  @Scheduled(initialDelayString =  "5000", fixedRateString = "5000")
  public void checkSyncStatus() {
    var positionGap = 0L;

    var globalLastPosition = statistics.lastBlockPosition();
    if(globalLastPosition.isPresent()) {
      var localLastPosition = blockService.getLatestBlock().getPosition();
      positionGap = Math.abs(globalLastPosition.get() - localLastPosition);
    }
    if(positionGap > MAX_BLOCKCHAIN_POSITION_GAP) {
      LOG.info("Gap of %d positions detected, resyncing!", positionGap);
      synchronizer.sync(globalLastPosition.get());
    }
  }
}
