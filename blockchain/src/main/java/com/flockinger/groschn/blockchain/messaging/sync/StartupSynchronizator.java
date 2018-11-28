package com.flockinger.groschn.blockchain.messaging.sync;

import com.flockinger.groschn.commons.exception.BlockchainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartupSynchronizator implements InitializingBean {

  @Autowired
  private FullSyncKeeper transactionFullSynchronizer;
  @Autowired
  private SmartBlockSynchronizer smartSynchronizer;
  @Autowired
  private GlobalBlockchainStatistics statistics;
  
  private final static Logger LOG = LoggerFactory.getLogger(StartupSynchronizator.class);
  
  @Override
  public void afterPropertiesSet()  {
    try {
      LOG.info("Starting startup-synchronization!");
      var lastPosition = statistics.lastBlockPosition();
      if(lastPosition.isPresent()) {
        smartSynchronizer.sync(lastPosition.get());
        transactionFullSynchronizer.fullSynchronization();
        LOG.info("Finished startup-synchronization successfully!");
      } else {
        LOG.warn("Unable to determine global last position, Full-Block-Synchronization aborted!");
      }
    } catch (BlockchainException e) {
      LOG.error("Node was unable to Initialize/Synchronize the Blockchain!", e);
    }
  }
}
