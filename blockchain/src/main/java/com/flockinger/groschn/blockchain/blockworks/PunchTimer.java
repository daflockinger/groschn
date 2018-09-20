package com.flockinger.groschn.blockchain.blockworks;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStatus;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;

@Component
public class PunchTimer {

  @Autowired
  private ConsensusFactory consensusFactory;
  @Autowired
  private BlockMaker blockMaker;
  @Autowired
  private SyncKeeper blockSynchronizer;
  
  @Value("${blockchain.punch-timer.process-timeout-seconds}")
  private Integer processTimeout;
  
  @Scheduled(initialDelayString="${blockchain.punch-timer.initial-delay}", 
      fixedRateString= "${blockchain.punch-timer.punch-rate}")
  public void checkMiningProcess() {
    if(isStillSynchronizingBlockchain()) {
      return;
    }
    if(consensusFactory.isProcessing()) {
      restartOnTimeout(checkIsProcessTimeouted());
    } else {
      blockMaker.produceBlock();
    }
  }
  
  private boolean isStillSynchronizingBlockchain() {
    return StringUtils.equals(blockSynchronizer.syncStatus(), SyncStatus.IN_PROGRESS.name());
  }
  
  private void restartOnTimeout(boolean isProcessTimeouted) {
    if(isProcessTimeouted) {
      consensusFactory.stopFindingConsensus();
      blockMaker.produceBlock();
    }
  }
  
  private boolean checkIsProcessTimeouted() {    
    Date lastProcess = consensusFactory.lastProcessStartDate()
        .orElse(new Date(0l));
    boolean isProecssRunningTooLongAlready = 
        DateUtils.addSeconds(lastProcess, processTimeout).before(new Date());
    return isProecssRunningTooLongAlready;
  }
}
