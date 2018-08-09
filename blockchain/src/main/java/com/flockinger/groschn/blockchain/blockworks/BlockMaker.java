package com.flockinger.groschn.blockchain.blockworks;

import com.flockinger.groschn.blockchain.model.Block;

public interface BlockMaker {

  final static Integer CURRENT_BLOCK_VERSION = 1;
  
  Block produceBlock();
}
