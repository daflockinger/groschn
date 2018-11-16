package com.flockinger.groschn.blockchain.messaging.sync.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.exception.TransactionAlreadyClearedException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.messaging.sync.FullSyncKeeper;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;

@Service
public class TransactionPoolFullSynchronizer implements FullSyncKeeper {

  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  private SyncInquirer inquirer;

  @Value("${atomix.node-id}")
  private String nodeId;

  private final static int TRANSACTION_POOL_PACKAGE_SIZE = 100;

  private final static Logger LOG = LoggerFactory.getLogger(BlockSynchronizer.class);
  private final SyncBatchRequest batchRequest =
      SyncBatchRequest.build().batchSize(TRANSACTION_POOL_PACKAGE_SIZE).idealReceiveNodeCount(3)
          .maxFetchRetries(2).topic(MainTopics.SYNC_TRANSACTIONS);


  //TODO maybe add clearing old raw transactions in pool before full sync!
  @Retryable(maxAttempts=3,backoff=@Backoff(delay=1000, multiplier=2))
  @Override
  public void fullSynchronization() {
    LOG.debug("Started full Transaction-Pool synchronization.");
    boolean hasFinishedSync = false;
    for (long packageNumber = 1l; !hasFinishedSync
        && packageNumber < (Long.MAX_VALUE / TRANSACTION_POOL_PACKAGE_SIZE); packageNumber++) {
      hasFinishedSync = storeBatchReturnHasFinished(
          SyncBatchRequest.build(batchRequest).fromPosition(packageNumber));
      LOG.debug("Successfully synced and stored %d Transactions.",
          packageNumber * TRANSACTION_POOL_PACKAGE_SIZE);
    }
    LOG.debug("Completed full Transaction-Pool synchronization.");
  }

  private boolean storeBatchReturnHasFinished(SyncBatchRequest request) {
    Optional<SyncResponse<Transaction>> response = fetchLongestResponse(request);
    response.stream().map(SyncResponse::getEntities).filter(Objects::nonNull)
        .forEach(this::storeGoodTransaction);
    return response.stream()
        .map(existingResponse -> existingResponse.isLastPositionReached()
            || ListUtils.emptyIfNull(existingResponse.getEntities()).isEmpty())
        .findFirst().orElse(true);
  }
  
  private Optional<SyncResponse<Transaction>> fetchLongestResponse(SyncBatchRequest request) {
    return inquirer.fetchNextBatch(request, Transaction.class).stream()
        .reduce(this::getBiggerBatch);
  }
  
  private <T extends Hashable<T>> SyncResponse<T> getBiggerBatch(SyncResponse<T> batchOne, SyncResponse<T> batchTwo) {
    return (batchOne.getEntities().size() >= batchTwo.getEntities().size()) ? batchOne : batchTwo;
  }

  private void storeGoodTransaction(List<Transaction> transactions) {
    for (Transaction transaction : transactions) {
      try {
        transactionManager.storeTransaction(transaction);
      } catch (AssessmentFailedException e) {
        LOG.warn("Skipping invalid Transaction during Full-Transaction-Synchronization!", e);
      } catch (TransactionAlreadyClearedException e) {
        LOG.info("Skip storing again already cleared Transaction!");
      }
    }
  }
}
