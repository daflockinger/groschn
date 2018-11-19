package com.flockinger.groschn.blockchain.messaging.sync;

import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;

public interface SmartBlockSynchronizer {

  void sync(SyncSettings settings);
}
