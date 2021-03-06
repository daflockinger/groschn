package com.flockinger.groschn.blockchain.consensus.impl;

import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.consensus.ConsensusAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.commons.hash.HashGenerator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  
  /**
   * Every PoW proven hash must start with a certain amount of those
   * characters (depending on the difficulty).
   */
  public final static String LEADING_ZERO = "0";
  
  @Autowired
  private BlockStorageService blockService;
  @Autowired
  private HashGenerator hashGenerator;
  
  private final Long STARTING_NONCE = 1l;
  
  private AtomicBoolean cancel = new AtomicBoolean(false);
  
  @Override
  public Optional<Block> reachConsensus(List<Transaction> transactions) {      
    cancel.set(false);
    return forgeBlock(createBaseBlock(transactions));
  }
  
  
  private Block createBaseBlock(List<Transaction> transactions) {    
    Block lastBlock = blockService.getLatestProofOfWorkBlock();
    Block freshBlock = new Block();
    freshBlock.setHash(null);
    freshBlock.setPosition(blockService.getLatestBlock().getPosition() + 1);
    freshBlock.setTransactions(transactions);
    freshBlock.setLastHash(lastBlock.getHash());
    freshBlock.setTimestamp(new Date().getTime());
    freshBlock.setVersion(BlockMaker.CURRENT_BLOCK_VERSION);
    freshBlock.setTransactionMerkleRoot(hashGenerator.calculateMerkleRootHash(transactions));
    
    Consent consent = new Consent();
    consent.setType(ConsensusType.PROOF_OF_WORK);
    consent.setDifficulty(determineNewDifficulty(lastBlock));
    freshBlock.setConsent(consent);
    return freshBlock;
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
  
  private Optional<Block> forgeBlock(Block freshBlock) {
    StopWatch miningTimer = StopWatch.createStarted();
    Consent consent = freshBlock.getConsent();
    String blockHash = "";
    consent.setTimestamp(new Date().getTime());
    Long nonceCount=STARTING_NONCE;
    while(!didWorkSucceed(blockHash, consent)) {
      if(cancel.get()) {
        return Optional.empty();
      }
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
    return Optional.of(freshBlock);
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
    return StringUtils.startsWith(blockHash, StringUtils.repeat(LEADING_ZERO, consent.getDifficulty()));
  }

  @Override
  public void stopFindingConsensus() {
    cancel.set(true);
  }
}
