package com.flockinger.groschn.blockchain.messaging.sync;

import com.flockinger.groschn.blockchain.messaging.dto.DeprecatedBlockInfoResult;

public interface SyncKeeper {
  
  void synchronize(DeprecatedBlockInfoResult infoResult);
  
  String syncStatus();
}
