package com.flockinger.groschn.blockchain.blockworks;

import com.flockinger.groschn.blockchain.exception.validation.ValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import java.util.List;

public interface BlockStorageService {
  
  StoredBlock saveInBlockchain(Block block) throws ValidationException;
  
  StoredBlock saveUnchecked(Block block);
  
  Block getLatestBlock();
  
  Block getLatestProofOfWorkBlock();
  
  Block getLatestProofOfWorkBlockBelowPosition(Long position);
  
  List<Block> findBlocks(long fromPosition, long quantity);
  
  void removeBlock(long position);
}
