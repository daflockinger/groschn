package com.flockinger.groschn.blockchain.validation.impl;

import com.flockinger.groschn.blockchain.model.Block;
import org.springframework.stereotype.Component;

@Component
public class InnerBlockValidator extends BlockValidator {

  @Override
  protected Block getLastBlock(long newBlockPosition) {
    long lastBlockPosition = Math.max(1,  newBlockPosition - 1);
    return blockService.findBlocks(lastBlockPosition, 1).stream().findFirst().get();
  }

  @Override
  protected void verifyBlockIsUsedByMajorityOfNodes(Block block) { }
}
