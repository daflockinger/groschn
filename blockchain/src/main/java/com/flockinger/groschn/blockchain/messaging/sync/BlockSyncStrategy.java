package com.flockinger.groschn.blockchain.messaging.sync;

import java.util.Optional;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.dto.SyncSettings;
import com.flockinger.groschn.blockchain.messaging.dto.SyncStrategyType;

public interface BlockSyncStrategy {
  
  Optional<BlockInfoResult> apply(SyncSettings settings);
  
  boolean isApplicable(SyncStrategyType startegy);
}
