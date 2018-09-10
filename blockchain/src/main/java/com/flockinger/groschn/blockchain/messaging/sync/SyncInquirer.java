package com.flockinger.groschn.blockchain.messaging.sync;

import java.util.Optional;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.model.Hashable;

/**
 * Fetches Synchronization packages for the requester.
 */
public interface SyncInquirer {

  <T extends Hashable> Optional<SyncResponse<T>> fetchNextBatch(SyncBatchRequest request,
      Class<T> responseType);
}
