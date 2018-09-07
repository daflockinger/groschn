package com.flockinger.groschn.blockchain.blockworks;

public interface BlockMaker {

  final static Integer CURRENT_BLOCK_VERSION = 1;
  
  void produceBlock();
}
