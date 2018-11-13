package com.flockinger.groschn.blockchain.blockworks;

import java.util.List;
import com.flockinger.groschn.blockchain.exception.validation.ValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;

public interface BlockStorageService {
  
  StoredBlock saveInBlockchain(Block block) throws ValidationException;
  
  StoredBlock saveUnchecked(Block block);
  
  Block getLatestBlock();
  
  Block getLatestProofOfWorkBlock();
  
  Block getLatestProofOfWorkBlockBelowPosition(Long position);
  
  List<Block> findBlocks(long fromPosition, long quantity);
  
  void removeBlocks(long fromPositionInclusive);
}
