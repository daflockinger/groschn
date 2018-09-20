package com.flockinger.groschn.blockchain.messaging.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.exception.BlockchainException;

@Component
public class StartupSynchronizator implements InitializingBean {
  
  @Autowired
  private FullSyncKeeper transactionFullSynchronizer;
  @Autowired
  private SyncDeterminator blockSynchronizer;
  
  private final static Logger LOG = LoggerFactory.getLogger(StartupSynchronizator.class);
  
  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      blockSynchronizer.determineAndSync();
      transactionFullSynchronizer.fullSynchronization();
    } catch (BlockchainException e) {
      LOG.error("Node was unable to Initialize/Synchronize the Blockchain!");
    }
  }
}
