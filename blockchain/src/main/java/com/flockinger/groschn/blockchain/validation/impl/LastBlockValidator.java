package com.flockinger.groschn.blockchain.validation.impl;

import com.flockinger.groschn.blockchain.messaging.sync.GlobalBlockchainStatistics;
import com.flockinger.groschn.blockchain.model.Block;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("lastBlockValidator")
public class LastBlockValidator extends BlockValidator {

  @Autowired
  private GlobalBlockchainStatistics globalStatistics;

  @Override
  protected Block getLastBlock(long newBlockPosition) {
    return blockService.getLatestBlock();
  }

  @Override
  protected void verifyBlockIsUsedByMajorityOfNodes(Block block) {
    boolean isBlockHashMostUsedOne = false;
    var overallHashes = globalStatistics.overallBlockHashes(block.getPosition());
    var mostUsedBlockHash = overallHashes.stream().collect(Collectors
        .groupingBy(Function.identity(), Collectors.counting()))
      .entrySet().stream()
        .max(Comparator.comparing(Entry::getValue))
        .map(Entry::getKey);
    if(mostUsedBlockHash.isPresent()) {
      isBlockHashMostUsedOne = StringUtils
          .equalsIgnoreCase(mostUsedBlockHash.get(), block.getHash());
    }
    verifyAssessment(isBlockHashMostUsedOne,
        "Block hash is not used by the majority of nodes in the Blockchain!");
  }
}
