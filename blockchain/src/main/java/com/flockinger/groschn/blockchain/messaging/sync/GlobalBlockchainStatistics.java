package com.flockinger.groschn.blockchain.messaging.sync;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GlobalBlockchainStatistics {

  @Autowired
  private SyncInquirer inquirer;

  private final static Logger LOG = LoggerFactory.getLogger(GlobalBlockchainStatistics.class);

  private final SyncBatchRequest request = SyncBatchRequest.build()
      .idealReceiveNodeCount(50)
      .maxFetchRetries(3)
      .topic(MainTopics.BLOCK_INFO);

  public Optional<Long> lastBlockPosition() {
    var syncResponses = inquirer.fetchNextBatch(request.fromPosition(1L).batchSize(1), BlockInfo.class);

    var lastPosition = syncResponses.stream()
        .map(SyncResponse::getLastPosition)
        .filter(Objects::nonNull)
        .collect(
            groupingBy(Function.identity(), counting())
        ).entrySet().stream()
        .max(comparing(Entry::getValue))
        .map(Entry::getKey);

    if(lastPosition.isPresent()) {
      LOG.info("Current Global last Block position: " + lastPosition.get());
    }
    return lastPosition;
  }
}
