package com.flockinger.groschn.blockchain.messaging.sync;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GlobalBlockchainStatistics {

  private final SyncInquirer inquirer;

  public GlobalBlockchainStatistics(SyncInquirer inquirer) {
    this.inquirer = inquirer;
  }

  private final static Logger LOG = LoggerFactory.getLogger(GlobalBlockchainStatistics.class);

  private final SyncBatchRequest request = SyncBatchRequest.build()
      .idealReceiveNodeCount(50)
      .maxFetchRetries(3)
      .batchSize(1)
      .topic(MainTopics.BLOCK_INFO);

  public Optional<Long> lastBlockPosition() {
    var syncResponses = inquirer.fetchNextBatch(request.fromPosition(1L), BlockInfo.class);

    var lastPosition = syncResponses.stream()
        .map(SyncResponse::getLastPosition)
        .filter(Objects::nonNull)
        .collect(
            groupingBy(Function.identity(), counting())
        ).entrySet().stream()
        .max(comparing(Entry::getValue))
        .map(Entry::getKey);

    lastPosition.ifPresent(aLong -> LOG.info("Current Global last Block position: " + aLong));
    return lastPosition;
  }

  public List<String> overallBlockHashes(Long forPosition) {
    var syncResponses = inquirer.fetchNextBatch(request.fromPosition(forPosition), BlockInfo.class);

    return syncResponses.stream()
        .map(SyncResponse::getEntities)
        .map(entities -> entities.stream()
            .filter(it -> Objects.equals(it.getPosition(), forPosition))
            .findFirst())
        .flatMap(Optional::stream)
        .map(BlockInfo::getBlockHash)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
