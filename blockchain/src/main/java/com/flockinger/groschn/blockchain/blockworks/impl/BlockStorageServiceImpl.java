package com.flockinger.groschn.blockchain.blockworks.impl;

import javax.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.exception.validation.BlockValidationException;
import com.flockinger.groschn.blockchain.exception.validation.ValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.impl.BlockValidator;

@Component
public class BlockStorageServiceImpl implements BlockStorageService {
   
  @Autowired
  private BlockValidator validator;
  @Autowired
  private BlockchainRepository dao;
  @Autowired
  private ModelMapper mapper;
  
  @PostConstruct
  public void initBlockchain() {
    if(dao.count() == 0) {
      StoredBlock genesisBlock = mapToStoredBlock(Block.GENESIS_BLOCK());
      dao.save(genesisBlock);
    }
  }
  
  @Override
  public StoredBlock saveInBlockchain(Block block) throws ValidationException {
    validateBlock(block);
    StoredBlock storedBlock = mapToStoredBlock(block);
    storedBlock = dao.save(storedBlock);
    return storedBlock;
  }
  
  private void validateBlock(Block block) {
    Assessment assessment = validator.validate(block);
    if(!assessment.isValid()) {
      throw new BlockValidationException("Block validation failed because of: " + 
            assessment.getReasonOfFailure());
    }
  }
  
  private StoredBlock mapToStoredBlock(Block block) {
    return mapper.map(block, StoredBlock.class);
  }
}
