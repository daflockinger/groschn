package com.flockinger.groschn.blockchain.consensus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.impl.MultiHashGenerator;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.config.GeneralConfig;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.PowConsent;
import com.flockinger.groschn.blockchain.consensus.model.ProofOfMajorityConsent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculatorTest;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@DataMongoTest
@ContextConfiguration(classes = { BlockchainRepository.class, ProofOfWorkAlgorithm.class, 
    MultiHashGenerator.class, MerkleRootCalculator.class})
@Import({GeneralConfig.class, CryptoConfig.class})
public class ProofOfWorkAlgorithmTest {
  
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
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof PowConsent);
    PowConsent consent = (PowConsent)forgedBlock.getConsent();
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
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof PowConsent);
    PowConsent consent = (PowConsent)forgedBlock.getConsent();
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
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof PowConsent);
    PowConsent consent = (PowConsent)forgedBlock.getConsent();
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
    assertTrue("verify consent is a proof of work consent", forgedBlock.getConsent() instanceof PowConsent);
    PowConsent consent = (PowConsent)forgedBlock.getConsent();
    assertEquals("verify consent difficulty stayed the same", 2, consent.getDifficulty().intValue());
    assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
    assertNotNull("verify consent has nonce", consent.getNonce());
    assertNotNull("verify consent has timestamp", consent.getTimestamp());
    assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }
  
  
  @After
  public void teardown() {
    dao.deleteAll();
  }
  
  private List<StoredBlock> fakeBlocks(long timeSpentMining) {
    StoredBlock block1 = new StoredBlock();
    block1.setPosition(100l);
    block1.setConsent(new ProofOfMajorityConsent());
    
    StoredBlock block2 = new StoredBlock();
    block2.setPosition(98l);
    block2.setConsent(new ProofOfMajorityConsent());
    
    StoredBlock block3 = new StoredBlock();
    block3.setPosition(97l);
    block3.setHash("0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    PowConsent powConsent = new PowConsent();
    powConsent.setDifficulty(2);
    powConsent.setMilliSecondsSpentMining(timeSpentMining);
    powConsent.setNonce(123l);
    powConsent.setTimestamp(2342343545l);
    block3.setConsent(powConsent);
    
    StoredBlock block4 = new StoredBlock();
    block4.setPosition(96l);
    block4.setConsent(new PowConsent());
    
    return ImmutableList.of(block1, block2, block3, block4);
  }
}
