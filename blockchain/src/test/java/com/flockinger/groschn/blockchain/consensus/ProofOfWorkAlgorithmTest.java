package com.flockinger.groschn.blockchain.consensus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.blockworks.impl.MultiHashGenerator;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculatorTest;
import com.google.common.collect.ImmutableList;


@ContextConfiguration(classes = { BlockchainRepository.class, ProofOfWorkAlgorithm.class, 
    MultiHashGenerator.class, MerkleRootCalculator.class})
public class ProofOfWorkAlgorithmTest extends BaseDbTest {
  
  @Autowired
  private BlockchainRepository dao;
  @Autowired
  private ProofOfWorkAlgorithm powAlgo;

  @Test
  public void testReachConsensus_withOldGenerationTimeTooFast_shouldReturnCorrect() {
    dao.insert(fakeBlocks(29999l));
    
    Block forgedBlock = powAlgo.reachConsensus(MerkleRootCalculatorTest.fakeTransactions(9, false));
    
    assertNotNull("verify that created block is not null", forgedBlock);
    assertEquals("verify correct block position", 101, forgedBlock.getPosition().longValue());
    assertEquals("verify block ", 
        "0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938", 
        forgedBlock.getLastHash());
    assertNotNull("verify block hash is there", forgedBlock.getHash());
    assertNotNull("verify block has timestamp", forgedBlock.getTimestamp());
    assertNotNull("verify block has merkle root", forgedBlock.getTransactionMerkleRoot());
    assertEquals("verify correct block version", 1, forgedBlock.getVersion().intValue());
    assertNotNull("verify block transactions are not null", forgedBlock.getTransactions());
    assertEquals("verify block transactions count", 9, forgedBlock.getTransactions().size());
    assertNotNull("verify block has a consent", forgedBlock.getConsent());
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof Consent);
    Consent consent = (Consent)forgedBlock.getConsent();
    assertEquals("verify consent difficulty increased", 3, consent.getDifficulty().intValue());
    assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
    assertNotNull("verify consent has nonce", consent.getNonce());
    assertNotNull("verify consent has timestamp", consent.getTimestamp());
    assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }
  
  @Test
  public void testReachConsensus_withOldGenerationTimeTooSlow_shouldReturnCorrect() {
    dao.insert(fakeBlocks(30001l));
    
    Block forgedBlock = powAlgo.reachConsensus(MerkleRootCalculatorTest.fakeTransactions(9, false));
    
    assertNotNull("verify that created block is not null", forgedBlock);
    assertEquals("verify correct block position", 101, forgedBlock.getPosition().longValue());
    assertEquals("verify block ", 
        "0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938", 
        forgedBlock.getLastHash());
    assertNotNull("verify block hash is there", forgedBlock.getHash());
    assertNotNull("verify block has timestamp", forgedBlock.getTimestamp());
    assertNotNull("verify block has merkle root", forgedBlock.getTransactionMerkleRoot());
    assertEquals("verify correct block version", 1, forgedBlock.getVersion().intValue());
    assertNotNull("verify block transactions are not null", forgedBlock.getTransactions());
    assertEquals("verify block transactions count", 9, forgedBlock.getTransactions().size());
    assertNotNull("verify block has a consent", forgedBlock.getConsent());
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof Consent);
    Consent consent = (Consent)forgedBlock.getConsent();
    assertEquals("verify consent difficulty decreased", 1, consent.getDifficulty().intValue());
    assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
    assertNotNull("verify consent has nonce", consent.getNonce());
    assertNotNull("verify consent has timestamp", consent.getTimestamp());
    assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }
  
  @Test
  public void testReachConsensus_withOldGenerationExact_shouldReturnCorrect() {
    dao.insert(fakeBlocks(30000l));
    
    Block forgedBlock = powAlgo.reachConsensus(MerkleRootCalculatorTest.fakeTransactions(9, false));
    
    assertNotNull("verify that created block is not null", forgedBlock);
    assertEquals("verify correct block position", 101, forgedBlock.getPosition().longValue());
    assertEquals("verify block ", 
        "0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938", 
        forgedBlock.getLastHash());
    assertNotNull("verify block hash is there", forgedBlock.getHash());
    assertNotNull("verify block has timestamp", forgedBlock.getTimestamp());
    assertNotNull("verify block has merkle root", forgedBlock.getTransactionMerkleRoot());
    assertEquals("verify correct block version", 1, forgedBlock.getVersion().intValue());
    assertNotNull("verify block transactions are not null", forgedBlock.getTransactions());
    assertEquals("verify block transactions count", 9, forgedBlock.getTransactions().size());
    assertNotNull("verify block has a consent", forgedBlock.getConsent());
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof Consent);
    Consent consent = (Consent)forgedBlock.getConsent();
    assertEquals("verify consent difficulty stayed the same", 2, consent.getDifficulty().intValue());
    assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
    assertNotNull("verify consent has nonce", consent.getNonce());
    assertNotNull("verify consent has timestamp", consent.getTimestamp());
    assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }
  
  
  @Test
  public void testReachConsensus_withForcingPossibleLongOverflow_shouldStillReturnCorrect() {
    dao.insert(fakeBlocks(30000l));
    
    Whitebox.setInternalState((ProofOfWorkAlgorithm)powAlgo, "STARTING_NONCE", Long.MAX_VALUE - 10);
    
    Block forgedBlock = powAlgo.reachConsensus(MerkleRootCalculatorTest.fakeTransactions(9, false));
    
    assertNotNull("verify that created block is not null", forgedBlock);
    assertEquals("verify correct block position", 101, forgedBlock.getPosition().longValue());
    assertEquals("verify block ", 
        "0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938", 
        forgedBlock.getLastHash());
    assertNotNull("verify block hash is there", forgedBlock.getHash());
    assertNotNull("verify block has timestamp", forgedBlock.getTimestamp());
    assertNotNull("verify block has merkle root", forgedBlock.getTransactionMerkleRoot());
    assertEquals("verify correct block version", 1, forgedBlock.getVersion().intValue());
    assertNotNull("verify block transactions are not null", forgedBlock.getTransactions());
    assertEquals("verify block transactions count", 9, forgedBlock.getTransactions().size());
    assertNotNull("verify block has a consent", forgedBlock.getConsent());
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof Consent);
    Consent consent = (Consent)forgedBlock.getConsent();
    assertEquals("verify consent difficulty stayed the same", 2, consent.getDifficulty().intValue());
    assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
    assertNotNull("verify consent has nonce", consent.getNonce());
    assertNotNull("verify consent has timestamp", consent.getTimestamp());
    assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }
  
  @Test
  public void testStopFindingConsensus_withRunningConsensusFinding_shouldStop() throws InterruptedException {
    dao.insert(fakeBlocksWithCustomDifficulty(30000l, 12));
    
    Thread consensusThread = new Thread(() -> {
      powAlgo.reachConsensus(MerkleRootCalculatorTest.fakeTransactions(9, false));
    });
    consensusThread.start();
    Thread.sleep(100l);
    powAlgo.stopFindingConsensus();
    
    Awaitility.await("verify it's canceled in reasonable time")
      .atMost(Duration.TWO_SECONDS)
      .until(this::isProcessingCanceled);
  }
  
  private boolean isProcessingCanceled() {
    return !powAlgo.isProcessing();
  }
  
  
  
  
  @After
  public void teardown() {
    dao.deleteAll();
  }
  
  private List<StoredBlock> fakeBlocks(long timeSpentMining) {
    return fakeBlocksWithCustomDifficulty(timeSpentMining, 2);
  }
  
  private List<StoredBlock> fakeBlocksWithCustomDifficulty(long timeSpentMining, int difficulty) {
    StoredBlock block1 = new StoredBlock();
    block1.setPosition(100l);
    Consent pom1 = new Consent();
    pom1.setType(ConsensusType.PROOF_OF_MAJORITY);
    block1.setConsent(pom1);
    
    StoredBlock block2 = new StoredBlock();
    block2.setPosition(98l);
    Consent pom2 = new Consent();
    pom2.setType(ConsensusType.PROOF_OF_MAJORITY);
    block2.setConsent(pom1);
    
    StoredBlock block3 = new StoredBlock();
    block3.setPosition(97l);
    block3.setHash("0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    Consent consent = new Consent();
    consent.setDifficulty(difficulty);
    consent.setMilliSecondsSpentMining(timeSpentMining);
    consent.setNonce(123l);
    consent.setTimestamp(2342343545l);
    consent.setType(ConsensusType.PROOF_OF_WORK);
    block3.setConsent(consent);
    
    StoredBlock block4 = new StoredBlock();
    block4.setPosition(96l);
    Consent consent2 = new Consent();
    consent2.setType(ConsensusType.PROOF_OF_WORK);
    block4.setConsent(consent2);
    
    return ImmutableList.of(block1, block2, block3, block4);
  }
}
