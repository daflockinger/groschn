package com.flockinger.groschn.blockchain.consensus;

import static com.flockinger.groschn.blockchain.consensus.model.PowConsent.MINING_RATE_MILLISECONDS;


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
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.PowConsent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;

@Component(value = "POW")
public class ProofOfWorkAlgorithm implements ConsensusAlgorithm {

  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private ModelMapper mapper;
  @Autowired
  private HashGenerator hashGenerator;
  @Autowired
  private MerkleRootCalculator merkleCalculator;

  @Override
  public Agreement reachConsensus(List<Transaction> transactions) {
    Block lastBlock = findLastBlock();
    
    Block freshBlock = new Block();
    freshBlock.setPosition(lastBlock.getPosition() + 1);
    freshBlock.setTransactions(transactions);
    freshBlock.setTimestamp(new Date().getTime());
    freshBlock.setVersion(BlockMaker.CURRENT_BLOCK_VERSION);
    freshBlock.setTransactionMerkleRoot(merkleCalculator.calculateMerkleRootHash(transactions));
    
    PowConsent consent = new PowConsent();
    consent.setDifficulty(determineNewDifficulty(lastBlock));
    freshBlock.setConsent(consent);
    
    StopWatch miningTimer = StopWatch.createStarted();
    mineBlock(freshBlock);
    miningTimer.stop();
    consent.setMilliSecondsSpentMining(miningTimer.getTime(TimeUnit.MILLISECONDS));
    
    
    return null;
  }
  
  private int determineNewDifficulty(Block lastBlock) {
    PowConsent consent = PowConsent.class.cast(lastBlock.getConsent());
    int difficulty = consent.getDifficulty();
    if(consent.getMilliSecondsSpentMining() > MINING_RATE_MILLISECONDS) {
      difficulty--;
    } else if (consent.getMilliSecondsSpentMining() < MINING_RATE_MILLISECONDS) {
      difficulty++;
    }
    return difficulty;
  }
  
  private void mineBlock(Block freshBlock) {
    PowConsent consent = PowConsent.class.cast(freshBlock.getConsent());
    String blockHash = "";
    consent.setTimestamp(new Date().getTime());
    Long nonceCount=1l;
    while(!didWorkSucceed(blockHash, consent)) {
      if(nonceCount == Long.MAX_VALUE) {
        consent.setTimestamp(new Date().getTime());
        nonceCount = 1l;
      } else {
        nonceCount++;
      }
      consent.setNonce(nonceCount);
      blockHash = hashGenerator.generateHash(freshBlock);
    }
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
  private boolean didWorkSucceed(String blockHash, PowConsent consent) {
    return StringUtils.startsWith(blockHash, StringUtils.repeat("0", consent.getDifficulty()));
  }
  
  private Block findLastBlock() {
    return blockDao
        .findFirst10ByConsentTypeOrderByPositionDesc(ConsensusType.PROOF_OF_WORK)
        .stream().map(dbBlock -> mapper.map(dbBlock, Block.class))
        .findFirst().get();
  }
}