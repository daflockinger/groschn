package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.model.Block;

class ScanResultMatcher {
  @Autowired
  private BlockStorageService blockService;
    
  public Optional<Long> findMatchingStartPosition(ScanningContext context) {
    var storedInfos = blockService.findBlocks(context.getFromPosition(), context.getBatchSize()).stream()
                              .map(this::mapToInfo).collect(Collectors.toList());
    Collections.sort(storedInfos);
    var receivedInfos = context.current().stream()
                                .map(BlockInfoResult::getBlockInfos)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
    Collections.sort(receivedInfos);
    
    return IntStream.range(0, storedInfos.size())
             .mapToObj(it -> it)
             .takeWhile(it -> it < receivedInfos.size())
             .takeWhile(it -> StringUtils.equals(storedInfos.get(it).getBlockHash(), receivedInfos.get(it).getBlockHash()))
             .map(it -> receivedInfos.get(it))
             .map(BlockInfo::getPosition)
             .reduce(Math::max);
  }
  
  private BlockInfo mapToInfo(Block block) {
    var info = new BlockInfo();
    info.setBlockHash(block.getHash());
    info.setPosition(block.getPosition());
    return info;
  }
}
