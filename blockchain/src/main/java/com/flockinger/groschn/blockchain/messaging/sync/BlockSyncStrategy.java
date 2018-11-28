package com.flockinger.groschn.blockchain.messaging.sync;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import java.util.Optional;

public interface BlockSyncStrategy {
  Optional<BlockInfoResult> apply(Long fromPosition);
}
