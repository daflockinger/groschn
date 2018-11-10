package com.flockinger.groschn.blockchain.blockworks;

import com.flockinger.groschn.blockchain.blockworks.dto.BlockGenerationStatus;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;

public interface BlockMaker {

  final static Integer CURRENT_BLOCK_VERSION = 1;
    
  void generation(BlockMakerCommand command);
  
  BlockGenerationStatus status();
}
