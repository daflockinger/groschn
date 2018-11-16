package com.flockinger.groschn.blockchain.messaging.sync;

import java.util.List;
import java.util.Optional;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResponse;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;

public interface ChainSelector {
  
  Optional<BlockInfoResult> choose(List<BlockInfoResponse> infoResponses);
  
}
