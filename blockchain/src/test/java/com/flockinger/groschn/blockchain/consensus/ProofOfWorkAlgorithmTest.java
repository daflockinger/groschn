package com.flockinger.groschn.blockchain.consensus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.impl.MultiHashGenerator;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculatorTest;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { ProofOfWorkAlgorithm.class, MultiHashGenerator.class, MerkleRootCalculator.class})
@Import(CryptoConfig.class)
public class ProofOfWorkAlgorithmTest {
  
  @Autowired
  private ProofOfWorkAlgorithm powAlgo;
  
  @MockBean
  private BlockStorageService mockStorage;

  @Test
  public void testReachConsensus_withOldGenerationTimeTooFast_shouldReturnCorrect() {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(29999l,0));
    mockOverallLastPosition();
    
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
  
  private void mockOverallLastPosition() {
    Block lastBlock = new Block();
    lastBlock.setPosition(100l);
    when(mockStorage.getLatestBlock()).thenReturn(lastBlock);
  }
  
  @Test
  public void testReachConsensus_withOldGenerationTimeTooSlow_shouldReturnCorrect() {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30001l,0));
    mockOverallLastPosition();
    
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
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30000l,0));
    mockOverallLastPosition();
    
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
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30000l,0));
    mockOverallLastPosition();
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
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30000l,12));
    mockOverallLastPosition();
    
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
  
  
  private Block fakeBlock(long timeSpentMining, int difficulty) {
    if(difficulty == 0) {
      difficulty = 2;
    }
    Block block = new Block();
    block.setPosition(97l);
    block.setHash("0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    Consent consent = new Consent();
    consent.setDifficulty(difficulty);
    consent.setMilliSecondsSpentMining(timeSpentMining);
    consent.setNonce(123l);
    consent.setTimestamp(2342343545l);
    consent.setType(ConsensusType.PROOF_OF_WORK);
    block.setConsent(consent);
    return block;
  }
}
