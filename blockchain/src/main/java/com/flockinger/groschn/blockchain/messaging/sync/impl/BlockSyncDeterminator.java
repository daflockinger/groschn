package com.flockinger.groschn.blockchain.messaging.sync.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;

@Service
public class BlockSyncDeterminator implements SyncDeterminator {
  
  @Autowired
  private BlockSynchronizer synchronizer;
  
  //TODO implement and test!!
  @Override
  public void determineAndSync(long proposedStartPosition) {

  }
}
