package com.flockinger.groschn.blockchain.messaging.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.commons.exception.BlockchainException;

@Component
public class StartupSynchronizator implements InitializingBean {
  
  @Autowired
  private FullSyncKeeper transactionFullSynchronizer;
  @Autowired
  private SmartBlockSynchronizer smartSynchronizer;
  @Autowired
  private BlockStorageService blockService;
  
  private final static Logger LOG = LoggerFactory.getLogger(StartupSynchronizator.class);
  
  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      LOG.info("Starting startup-synchronization!");
      smartSynchronizer.sync(SyncSettings.scan(blockService.getLatestBlock().getPosition()));
      transactionFullSynchronizer.fullSynchronization();
      LOG.info("Finished startup-synchronization successfully!");
    } catch (BlockchainException e) {
      LOG.error("Node was unable to Initialize/Synchronize the Blockchain!", e);
    }
  }
}
