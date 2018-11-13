package com.flockinger.groschn.blockchain.validation.impl;

import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.model.Block;

@Component
public class InnerBlockValidator extends BlockValidator {
  
  protected Block getLastBlock(long newBlockPosition) {
    long lastBlockPosition = Math.max(1,  newBlockPosition - 1);
    return blockService.findBlocks(lastBlockPosition, 1).stream().findFirst().get();
  }
}
