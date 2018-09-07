package com.flockinger.groschn.blockchain.blockworks;

import com.flockinger.groschn.blockchain.exception.validation.ValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;

public interface BlockStorageService {
  
  StoredBlock saveInBlockchain(Block block) throws ValidationException;
  
  Block getLatestBlock();
  
  Block getLatestProofOfWorkBlock();
}
