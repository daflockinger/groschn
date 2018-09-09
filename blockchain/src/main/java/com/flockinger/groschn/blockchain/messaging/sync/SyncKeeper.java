package com.flockinger.groschn.blockchain.messaging.sync;

public interface SyncKeeper {
  
  void syncronize(Long fromPosition);
}
