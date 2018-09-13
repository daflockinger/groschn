package com.flockinger.groschn.blockchain.messaging.sync;

public interface SyncKeeper {
  
  void synchronize(long fromPosition);
}
