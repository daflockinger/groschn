package com.flockinger.groschn.blockchain.messaging.sync;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;

public interface SyncKeeper {
  
  void synchronize(BlockInfoResult infoResult);
  
  String syncStatus();
}
