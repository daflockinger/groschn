package com.flockinger.groschn.blockchain.blockworks;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockGenerationStatus;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;

@Component
public class PunchTimer {

  @Autowired
  private BlockMaker blockMaker;
  @Autowired
  private SyncKeeper blockSynchronizer;

  private final static Logger LOG = LoggerFactory.getLogger(PunchTimer.class);

  @Value("${blockchain.punch-timer.process-timeout-seconds}")
  private Integer processTimeout;

  @Scheduled(initialDelayString = "${blockchain.punch-timer.initial-delay}",
      fixedRateString = "${blockchain.punch-timer.punch-rate}")
  public void checkMiningProcess() {
    if (isStillSynchronizingBlockchain() || isRunningOrStopped()) {
      return;
    }

    LOG.info("Start forging Block.");
    try {
      blockMaker.generation(BlockMakerCommand.RESTART);
    } catch (RuntimeException e) {
      LOG.error("Something unexpected happened while forging fresh block!", e);
    }
  }

  private boolean isRunningOrStopped() {
    var status = blockMaker.status();
    return BlockGenerationStatus.RUNNING.equals(status)
        || BlockGenerationStatus.STOPPED.equals(status);
  }

  private boolean isStillSynchronizingBlockchain() {
    return StringUtils.equals(blockSynchronizer.syncStatus(), SyncStatus.IN_PROGRESS.name());
  }
}
