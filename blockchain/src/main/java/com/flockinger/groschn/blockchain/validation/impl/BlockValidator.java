package com.flockinger.groschn.blockchain.validation.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.AssessmentFailure;
import com.flockinger.groschn.blockchain.validation.ConsentValidator;
import com.flockinger.groschn.blockchain.validation.Validator;

@Component
public class BlockValidator implements Validator<Block> {

  @Autowired
  @Qualifier("BlockTransaction_Validator")
  private Validator<List<Transaction>> transactionValidator;
  @Autowired
  private List<ConsentValidator> consensusValidators;
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private HashGenerator hasher;
  @Autowired
  private MerkleRootCalculator merkleRootCalculator;
  @Autowired
  private CompressionUtils compressor;

  @Override
  public Assessment validate(Block value) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new Block:
    try {
      Block lastBlock = blockService.getLatestBlock();
      
      // 1. check if position is exactly one higher than existing one
      isPositionExactlyOneHigher(value.getPosition(), lastBlock);
      // 2. check if lastHash is correct 
      verifyLastHash(value.getLastHash(), lastBlock);
      // 4. check if transaction merkleRoot-Hash is correct
      verifyTransactionsMerkleRoot(value);
      // 5. check if timestamp is in the past but not too much (set limit for that maybe 2 hours like bitcoin or less)
      verifyTimestamp(value.getTimestamp());
      // 6. check if version is correct
      verifyVersion(value.getVersion());
      // 7. check max transaction size
      checkTransactionSize(value.getTransactions());
      // 8. call consent validation
      validateConsensus(value);
      // 9. call transaction validations
      validateTransactions(value.getTransactions());
      // 3. verify if current hash is correctly calculated
      verifyCurrentHash(value);
      isBlockValid.setValid(true);
    } catch (AssessmentFailedException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
      isBlockValid.setFailure(e.getFailure());
    } catch (BlockchainException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }   
    return isBlockValid;
  }
  
  private void isPositionExactlyOneHigher(Long position, Block lastBlock) {
    verifyAssessment(position != null && position > lastBlock.getPosition(), 
        "Incomming block must have a higher position than the latest one!");
    
    verifyAssessment(position != null && position <= (lastBlock.getPosition() + 1), 
        "Incomming block has a too hight position, please resynchronize!", 
        AssessmentFailure.BLOCK_POSITION_TOO_HIGH);
  }
  
  private void verifyLastHash(String lastHash, Block lastBlock) {
    lastBlock.setHash(null);
    verifyAssessment(hasher.isHashCorrect(lastHash, lastBlock), "Last block hash is wrong, try reynchronizing!", 
        AssessmentFailure.BLOCK_LAST_HASH_WRONG);
  }
  
  private void verifyCurrentHash(Block block) {
    String currentHash = block.getHash();
    block.setHash(null);
    boolean isHashCorrect = hasher.isHashCorrect(currentHash, block);
    block.setHash(currentHash);
    
    verifyAssessment(isHashCorrect, "Block hash is wrong!");
  }
  
  private void verifyTransactionsMerkleRoot(Block value) {
    String rootHash = merkleRootCalculator.calculateMerkleRootHash(value.getTransactions());
    verifyAssessment(rootHash.equals(value.getTransactionMerkleRoot()), 
        "MerkleRoot-Hash of all transactions is wrong!");
  }
  
  private void verifyTimestamp(Long timestamp) {
    Long now = new Date().getTime();
    verifyAssessment(timestamp != null && timestamp <= now, 
        "Blocks cannot be dated in the future!");
  }
  
  private void verifyVersion(Integer version) {
    verifyAssessment(version != null && BlockMaker.CURRENT_BLOCK_VERSION == version, 
        "Version is not valid for this client: " + version);
  }
  
  private void checkTransactionSize(List<Transaction> transactions) {
    int compressedSize = compressor.compressedByteSize(transactions);
    verifyAssessment(compressedSize <= Block.MAX_TRANSACTION_BYTE_SIZE, 
        "Max compressed transaction size exceeded: " + compressedSize);
  }
  
  private void validateConsensus(Block block) {
    ConsentValidator consentValidator = consensusValidators.stream().filter(validator -> validator.type()
        .equals(block.getConsent().getType())).findFirst().get();
    verifyAssessment(consentValidator.validate(block));
  }
  
  private void validateTransactions(List<Transaction> transactions) {
    verifyAssessment(transactionValidator.validate(transactions));
  }
  
  private void verifyAssessment(Assessment assessment) {
    verifyAssessment(assessment.isValid(), assessment.getReasonOfFailure());
  }
  
  private void verifyAssessment(boolean isValid, String errorMessage) {
    verifyAssessment(isValid, errorMessage, AssessmentFailure.NONE);
  }
  private void verifyAssessment(boolean isValid, String errorMessage, AssessmentFailure failure) {
    if(!isValid) {
      throw new AssessmentFailedException(errorMessage, failure);
    }
  }
}
