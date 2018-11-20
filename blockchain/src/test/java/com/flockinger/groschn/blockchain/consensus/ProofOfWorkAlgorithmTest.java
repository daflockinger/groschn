package com.flockinger.groschn.blockchain.consensus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.commons.config.CommonsConfig;
import com.google.common.collect.ImmutableList;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ProofOfWorkAlgorithm.class})
@Import({CryptoConfig.class, TestConfig.class, CommonsConfig.class})
public class ProofOfWorkAlgorithmTest {

  @Autowired
  private ProofOfWorkAlgorithm powAlgo;

  @MockBean
  private BlockStorageService mockStorage;

  @Before
  public void setup() {
    Whitebox.setInternalState((ProofOfWorkAlgorithm) powAlgo, "STARTING_NONCE", 1l);
  }

  @Test
  public void testReachConsensus_withOldGenerationTimeTooFast_shouldReturnCorrect() {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(29999l, 0));
    mockOverallLastPosition();

    var forgedBlock = powAlgo.reachConsensus(fakeTransactions(9, false)).get();

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
    assertTrue("verify consent is a proof of work consent",
        forgedBlock.getConsent() instanceof Consent);
    Consent consent = (Consent) forgedBlock.getConsent();
    assertEquals("verify consent difficulty increased", 2, consent.getDifficulty().intValue());
    assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
    assertNotNull("verify consent has nonce", consent.getNonce());
    assertNotNull("verify consent has timestamp", consent.getTimestamp());
    assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }


  @Test
  public void testReachConsensus_withDoingItTwice_shouldReturnCorrect() {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(29999l, 0));
    mockOverallLastPosition();
    var block = powAlgo.reachConsensus(fakeTransactions(9, false)).get();

    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(block);
    Block lastBlock = new Block();
    lastBlock.setPosition(101l);
    when(mockStorage.getLatestBlock()).thenReturn(lastBlock);

    var secondBlock = powAlgo.reachConsensus(fakeTransactions(9, false)).get();
    assertEquals("verify that last hash of second forged block is equal to hash of first block",
        block.getHash(), secondBlock.getLastHash());
  }

  private void mockOverallLastPosition() {
    Block lastBlock = new Block();
    lastBlock.setPosition(100l);
    when(mockStorage.getLatestBlock()).thenReturn(lastBlock);
  }

  @Test
  public void testReachConsensuslatch_withOldGenerationTimeTooSlow_shouldReturnCorrect() {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30001l, 0));
    mockOverallLastPosition();

    var forgedBlock = powAlgo.reachConsensus(fakeTransactions(9, false)).get();

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
      assertTrue("verify consent is a proof of work consent",
          forgedBlock.getConsent() instanceof Consent);
      Consent consent = (Consent) forgedBlock.getConsent();
      assertEquals("verify consent difficulty decreased", 0, consent.getDifficulty().intValue());
      assertNotNull("verify consent has timestamp", consent.getTimestamp());
      assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }

  @Test
  public void testReachConsensus_withOldGenerationExact_shouldReturnCorrect() {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30000l, 0));
    mockOverallLastPosition();

    var forgedBlock = powAlgo.reachConsensus(fakeTransactions(9, false)).get();

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
      assertTrue("verify consent is a proof of work consent",
          forgedBlock.getConsent() instanceof Consent);
      Consent consent = (Consent) forgedBlock.getConsent();
      assertEquals("verify consent difficulty stayed the same", 1,
          consent.getDifficulty().intValue());
      assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
      assertNotNull("verify consent has nonce", consent.getNonce());
      assertNotNull("verify consent has timestamp", consent.getTimestamp());
      assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }


  @Test
  public void testReachConsensus_withForcingPossibleLongOverflow_shouldStillReturnCorrect() {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30000l, 0));
    mockOverallLastPosition();
    Whitebox.setInternalState((ProofOfWorkAlgorithm) powAlgo, "STARTING_NONCE",
        Long.MAX_VALUE - 10);

    var forgedBlock = powAlgo.reachConsensus(fakeTransactions(9, false)).get();

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
      assertTrue("verify consent is a proof of work consent",
          forgedBlock.getConsent() instanceof Consent);
      Consent consent = (Consent) forgedBlock.getConsent();
      assertEquals("verify consent difficulty stayed the same", 1,
          consent.getDifficulty().intValue());
      assertNotNull("verify consent has time spent value", consent.getMilliSecondsSpentMining());
      assertNotNull("verify consent has nonce", consent.getNonce());
      assertNotNull("verify consent has timestamp", consent.getTimestamp());
      assertEquals("verify correct consent type", ConsensusType.PROOF_OF_WORK, consent.getType());
  }


  @Test
  public void testStopFindingConsensus_withRunningConsensusFinding_shouldStop()
      throws InterruptedException, ExecutionException, TimeoutException {
    when(mockStorage.getLatestProofOfWorkBlock()).thenReturn(fakeBlock(30000l, 12));
    mockOverallLastPosition();
    
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    var block = executorService.submit(new Callable<Optional<Block>>() {
      @Override
      public Optional<Block> call() throws Exception {
        return powAlgo.reachConsensus(fakeTransactions(9, false));
      }});
    Thread.sleep(200);
    powAlgo.stopFindingConsensus();
    
    var generatedBlock = block.get(1, TimeUnit.SECONDS);
    assertFalse("verify block of stopped conesnsus is empty", generatedBlock.isPresent());
  }


  private Block fakeBlock(long timeSpentMining, int difficulty) {
    if (difficulty == 0) {
      difficulty = 1;
    }
    Block block = new Block();
    block.setPosition(97l);
    block.setHash(
        "0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    Consent consent = new Consent();
    consent.setDifficulty(difficulty);
    consent.setMilliSecondsSpentMining(timeSpentMining);
    consent.setNonce(123l);
    consent.setTimestamp(2342343545l);
    consent.setType(ConsensusType.PROOF_OF_WORK);
    block.setConsent(consent);
    return block;
  }

  public static List<Transaction> fakeTransactions(int size, boolean modifyLittleStuff) {
    Transaction tra1 = new Transaction();
    tra1.setInputs(statementList(fakeInput(86l), fakeInput(14l)));
    tra1.setOutputs(statementList(fakeInput(27l), fakeInput(73l)));
    tra1.setLockTime(934857l);
    tra1.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra2 = new Transaction();
    tra2.setInputs(statementList(fakeInput(6l), fakeInput(4l)));
    tra2.setOutputs(statementList(fakeInput(7l), fakeInput(3l)));
    tra2.setLockTime(87687l);
    tra2.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra3 = new Transaction();
    tra3.setInputs(statementList(fakeInput(9996l), fakeInput(9994l)));
    tra3.setOutputs(statementList(fakeInput(9997l), fakeInput(9993l)));
    tra3.setLockTime(432l);
    tra3.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra4 = new Transaction();
    tra4.setInputs(statementList(fakeInput(670006l), fakeInput(670004l)));
    tra4.setOutputs(statementList(fakeInput(670007l), fakeInput(670003l)));
    tra4.setLockTime(987l);
    tra4.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra5 = new Transaction();
    tra5.setInputs(statementList(fakeInput(3406l), fakeInput(3404l)));
    tra5.setOutputs(statementList(fakeInput(3407l), fakeInput(3403l)));
    tra5.setLockTime(46547l);
    tra5.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra6 = new Transaction();
    tra6.setInputs(statementList(fakeInput(106l), fakeInput(104l)));
    tra6.setOutputs(statementList(fakeInput(107l), fakeInput(103l)));
    tra6.setLockTime(798678l);
    tra6.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra7 = new Transaction();
    tra7.setInputs(statementList(fakeInput(6006l), fakeInput(6004l)));
    tra7.setOutputs(statementList(fakeInput(6007l), fakeInput(6003l)));
    tra7.setLockTime(5423454l);
    tra7.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra8 = new Transaction();
    tra8.setInputs(statementList(fakeInput(5006l), fakeInput(5004l)));
    tra8.setOutputs(statementList(fakeInput(5007l), fakeInput(5003l)));
    tra8.setLockTime(5423454l);
    tra8.setTransactionHash(UUID.randomUUID().toString());

    Transaction tra9 = new Transaction();
    tra9.setInputs(statementList(fakeInput(4006l), fakeInput(4004l)));
    tra9.setOutputs(statementList(fakeInput(4007l), fakeInput(4003l)));
    tra9.setLockTime(5423454l);
    tra9.setTransactionHash(UUID.randomUUID().toString());

    if (modifyLittleStuff) {
      tra9.getOutputs().get(0).setTimestamp(1234568l);
    }

    List<Transaction> transactions = new ArrayList<>();
    transactions.addAll(ImmutableList.of(tra1, tra2, tra3, tra4, tra5));
    transactions.addAll(ImmutableList.of(tra6, tra7, tra8, tra9));
    List<Transaction> minList = new ArrayList<>();
    minList.addAll(transactions.subList(0, size));
    return minList;
  }

  private static <T extends TransactionOutput> List<T> statementList(T... statements) {
    return Arrays.asList(statements);
  }

  public static TransactionInput fakeInput(long amount) {
    TransactionInput input = new TransactionInput();
    input.setAmount(new BigDecimal(amount));
    input.setPublicKey("keykey");
    input.setTimestamp(1234567l);

    return input;
  }
}
