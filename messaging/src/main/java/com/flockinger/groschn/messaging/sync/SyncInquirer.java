package com.flockinger.groschn.messaging.sync;

import java.util.List;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;

/**
 * Fetches Synchronization packages for the requester.
 */
public interface SyncInquirer {

  <T extends Hashable<T>> List<SyncResponse<T>> fetchNextBatch(SyncBatchRequest request,
      Class<T> responseType);
}
