package com.flockinger.groschn.blockchain.messaging.sync;

public interface SmartBlockSynchronizer {

  void sync(Long fromPosition);
}
