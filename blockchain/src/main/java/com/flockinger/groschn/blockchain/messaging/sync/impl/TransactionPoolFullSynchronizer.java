package com.flockinger.groschn.blockchain.messaging.sync.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.SyncKeeper;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.github.benmanes.caffeine.cache.Cache;

@Service
public class TransactionPoolFullSynchronizer implements SyncKeeper {
  
  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  @Qualifier("SyncTransactionId_Cache")
  private Cache<String, String> syncBlockIdCache;
  @Autowired
  private SyncInquirer inquirer;
  
  @Value("${blockchain.node.id}")
  private String nodeId;
  
  private final static int TRANSACTION_POOL_PACKAGE_SIZE = 100;
  
  private final static Logger LOG = LoggerFactory.getLogger(BlockFullSynchronizer.class);
  private final SyncBatchRequest batchRequest = SyncBatchRequest.build()
      .batchSize(TRANSACTION_POOL_PACKAGE_SIZE)
      .idealReceiveNodeCount(3)
      .maxFetchRetries(2)
      .topic(MainTopics.SYNC_TRANSACTIONS);
  
  
  //TODO continue there!
  @Override
  public void synchronize(long fromPosition) {
    LOG.debug("Started synchronization at position: " + fromPosition);
      boolean hasFinishedSync = false;
      
      while(!hasFinishedSync) {
        hasFinishedSync = storeBatchReturnHasFinished(SyncBatchRequest.build(batchRequest).fromPosition(fromPosition));
        LOG.debug("Successfully synced and stored Transactions until position: " + fromPosition);
        fromPosition += TRANSACTION_POOL_PACKAGE_SIZE;
      }
      
  }
  
  private boolean storeBatchReturnHasFinished(SyncBatchRequest request) {
    Optional<SyncResponse<Transaction>> response = Optional.empty();
    boolean isValidAndStored = false;
      response = inquirer.fetchNextBatch(request, Transaction.class);
      isValidAndStored = response.stream().map(SyncResponse::getEntities)
          .filter(Objects::nonNull)
          .map(this::storeTransactionAndReturnSuccess)
          .findFirst().orElse(true);
    return response.stream().map(existingResponse -> 
    existingResponse.isLastPositionReached() || emptyIfNull(existingResponse.getEntities()).isEmpty())
        .findFirst().orElse(true) || !isValidAndStored;
  }
  
  
  private boolean storeTransactionAndReturnSuccess(List<Transaction> transactions) {
    boolean allStoredSuccessful = false;
  /*  Collections.sort(blocks, Comparator.comparing(Transaction::getPosition));
    try {
      for(Block block: blocks) {
        transactionManager.saveInBlockchain(block);
      }
      allStoredSuccessful = true;
    } catch(BlockchainException e) {
      LOG.error("Received invalid Block during Block-Synchronization!", e);
    }*/
    return allStoredSuccessful;
  }
}
