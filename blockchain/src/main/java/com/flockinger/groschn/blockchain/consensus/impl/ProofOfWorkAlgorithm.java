package com.flockinger.groschn.blockchain.consensus.impl;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;

@Component(value = "POW")
public class ProofOfWorkAlgorithm implements ConsensusAlgorithm {

  /**
   * Average time it should take to mine one block.
   */
  public final static Long MINING_RATE_MILLISECONDS = 30l * 1000l;
  
  /**
   * Default difficulty value for the very beginning.
   */
  public final static Integer DEFAULT_DIFFICULTY = 4;
  
  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private ModelMapper mapper;
  @Autowired
  private HashGenerator hashGenerator;
  @Autowired
  private MerkleRootCalculator merkleCalculator;
  
  private final Long STARTING_NONCE = 1l;
  
  private boolean processingConsent = false;

  @Override
  public Block reachConsensus(List<Transaction> transactions) {
    processingConsent = true;
    Block lastBlock = findLastBlock();
    
    Block freshBlock = new Block();
    freshBlock.setPosition(getLastPosition() + 1);
    freshBlock.setTransactions(transactions);
    freshBlock.setLastHash(lastBlock.getHash());
    freshBlock.setTimestamp(new Date().getTime());
    freshBlock.setVersion(BlockMaker.CURRENT_BLOCK_VERSION);
    freshBlock.setTransactionMerkleRoot(merkleCalculator.calculateMerkleRootHash(transactions));
    
    Consent consent = new Consent();
    consent.setType(ConsensusType.PROOF_OF_WORK);
    consent.setDifficulty(determineNewDifficulty(lastBlock));
    freshBlock.setConsent(consent);
    
    forgeBlock(freshBlock);
    processingConsent = false;
    return freshBlock;
  }
  
  private long getLastPosition() {
    return blockDao.findFirstByOrderByPositionDesc().get().getPosition();
  }
  
  private int determineNewDifficulty(Block lastBlock) {
    Consent consent = Consent.class.cast(lastBlock.getConsent());
    int difficulty = consent.getDifficulty();
    if(consent.getMilliSecondsSpentMining() > MINING_RATE_MILLISECONDS) {
      difficulty--;
    } else if (consent.getMilliSecondsSpentMining() < MINING_RATE_MILLISECONDS) {
      difficulty++;
    }
    return difficulty;
  }
  
  private void forgeBlock(Block freshBlock) {
    StopWatch miningTimer = StopWatch.createStarted();
    Consent consent = freshBlock.getConsent();
    String blockHash = "";
    consent.setTimestamp(new Date().getTime());
    Long nonceCount=STARTING_NONCE;
    while(!didWorkSucceed(blockHash, consent) && processingConsent) {
      if(nonceCount == Long.MAX_VALUE) {
        consent.setTimestamp(new Date().getTime());
        nonceCount = STARTING_NONCE;
      } else {
        nonceCount++;
      }
      consent.setNonce(nonceCount);
      consent.setMilliSecondsSpentMining(miningTimer.getTime(TimeUnit.MILLISECONDS));
      blockHash = hashGenerator.generateHash(freshBlock);
    }
    miningTimer.stop();
    freshBlock.setHash(blockHash);
  }
  
  /**
   * Returns true if the hash contains at least the amount of leading <BR>
   * zeros as specified in the PowerConsents difficulty value.
   * 
   * @param blockHash
   * @param consent
   * @return
   */
  private boolean didWorkSucceed(String blockHash, Consent consent) {
    return StringUtils.startsWith(blockHash, StringUtils.repeat("0", consent.getDifficulty()));
  }
  
  private Block findLastBlock() {
    
    //TODO replace by block Storage service!!
    return blockDao
        .findTop3ByConsentTypeOrderByPositionDesc(ConsensusType.PROOF_OF_WORK)
        .stream().map(dbBlock -> mapper.map(dbBlock, Block.class))
        .findFirst().get();
  }

  @Override
  public void stopFindingConsensus() {
    processingConsent = false;
  }

  @Override
  public boolean isProcessing() {
    return processingConsent;
  }
}
