package com.flockinger.groschn.blockchain.transaction;

import static com.flockinger.groschn.blockchain.TestDataFactory.createRandomTransactionInputWith;
import static com.flockinger.groschn.blockchain.TestDataFactory.createRandomTransactionOutputWith;
import static com.flockinger.groschn.blockchain.TestDataFactory.createRandomTransactionWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.api.dto.TransactionIdDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionStatusDto;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.dto.TransactionStatementDto;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.exception.TransactionAlreadyClearedException;
import com.flockinger.groschn.blockchain.exception.TransactionNotFoundException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionInput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionOutput;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionManagerImpl;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.commons.TransactionUtils;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.exception.crypto.CantConfigureSigningAlgorithmException;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {TransactionManagerImpl.class, TransactionPoolRepository.class, MongoDbFactory.class,
    BlockchainRepository.class})
public class TransactionManagerTest extends BaseDbTest {

  @Autowired
  private TransactionManager manager;

  @Autowired
  private Compressor compressor;

  @MockBean
  private TransactionUtils transactionUtils;
  @MockBean
  private WalletService walletMock;
  @MockBean(name = "Transaction_Validator")
  private Validator<Transaction> validator;
  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private TransactionPoolRepository poolDao;
  
  private List<StoredPoolTransaction> fakePoolTransactions = createFakePooledTransactions();
  
  @Autowired 
  private ModelMapper mapper;
  
  
  @Before
  public void setup() {
    when(walletMock.getNodePublicKey()).thenReturn("masta-key");
    poolDao.deleteAll();
    blockDao.deleteAll();
  }
  
  @Test
  public void testFetchTransactionsFromPool_withSomeByteSize_shouldReturnCorrect() {
    poolDao.saveAll(fakePoolTransactions);
    List<Transaction> transactions = manager.fetchTransactionsBySize(5000);
    long size = transactions.stream()
        .map(compressor::compress)
        .mapToLong(entity -> entity.getEntity().length)
        .sum();
    assertNotNull("verify returned transactions are not null", transactions);
    assertFalse("verify returned transactions are not empty", transactions.isEmpty());
    assertTrue("verify that returned transactions are smaller than the limit", size < 5000);
    assertEquals("verify first returned transaction should be oldest raw one", "993", transactions.get(0).getTransactionHash());
    assertEquals("verify second returned transaction should be 2nd oldest raw one", "995", transactions.get(1).getTransactionHash());
    assertEquals("verify third returned transaction should be 3rd oldest raw one", "998", transactions.get(2).getTransactionHash());
    assertEquals("verify fourth returned transaction should be 4th oldest raw one", "999", transactions.get(3).getTransactionHash());
    
  }
  
  @Test
  public void testFetchTransactionsFromPool_withByteSizeZero_shouldReturnEmpty() {
    poolDao.saveAll(fakePoolTransactions);
    
    List<Transaction> transactions = manager.fetchTransactionsBySize(0);
    assertNotNull("verify returned transactions are not null", transactions);
    assertTrue("verify returned transactions are empty", transactions.isEmpty());
  }
  
  @Test
  public void testFetchTransactionsPaginated_withNormalValues_shouldWork() {
    poolDao.saveAll(fakePoolTransactions);
    List<Transaction> transactions = manager.fetchTransactionsPaginated(1, 2);

    assertNotNull("verify returned transactions are not null", transactions);
    assertEquals("verify that returned transaction size is correct", 2, transactions.size());
    assertEquals("verify third returned transaction should be 3rd oldest raw one", "998", transactions.get(0).getTransactionHash());
    assertEquals("verify fourth returned transaction should be 4th oldest raw one", "999", transactions.get(1).getTransactionHash());
    
    List<Transaction> transactions2 = manager.fetchTransactionsPaginated(1, 3);
    assertNotNull("verify returned transactions are not null", transactions2);
    assertEquals("verify that returned transaction size is correct", 1, transactions2.size());
    assertEquals("verify fourth returned transaction should be 4th oldest raw one", "999", transactions2.get(0).getTransactionHash());
  }
  
  @Test
  public void testFetchTransactionsPaginated_withZeroValues_shouldAtLeastONe() {
    poolDao.saveAll(fakePoolTransactions);
    
    List<Transaction> transactions = manager.fetchTransactionsPaginated(0, 0);
    assertNotNull("verify returned transactions are not null", transactions);
    assertEquals("verify that returned transaction size is correct", 1, transactions.size());
  }
  
  @Test
  public void testFetchTransactionsPaginated_withTooHighValues_shouldReturnEmpty() {
    poolDao.saveAll(fakePoolTransactions);
    
    List<Transaction> transactions = manager.fetchTransactionsPaginated(10, 100);
    assertNotNull("verify returned transactions are not null", transactions);
    assertEquals("verify that returned transaction size is correct", 0, transactions.size());
  }
  
  
  @Test
  public void testCreateSignedTransaction_withValidRequest_shouldCreateSignedTransactionCorrectly() {
    // mock
    when(transactionUtils.generateHash(any())).thenReturn("some hash");
    when(transactionUtils.sign(any(), any())).thenReturn("x0x0x0");
    StoredTransactionOutput requestOutput = new StoredTransactionOutput();
    requestOutput.setAmount(new BigDecimal("2000.2"));
    requestOutput.setPublicKey("masterkey");
    requestOutput.setSequenceNumber(2l);
    requestOutput.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(requestOutput, "bestHashEver"));
    // given124l
    TransactionDto request = new TransactionDto();
    TransactionStatementDto requestInput  = new TransactionStatementDto();
    requestInput.setAmount(999d);
    requestInput.setPublicKey("masterkey");
    requestInput.setSequenceNumber(1l);
    requestInput.setTimestamp(3000l);
    request.setInputs(ImmutableList.of(requestInput));
    request.setOutputs(ImmutableList.of(createStatement(1),createStatement(2),createStatement(3)));
    // execute
    Transaction signedTransaction = manager.createSignedTransaction(request);
    // assert
    assertNotNull("verify signed transaction is not null", signedTransaction);
    assertEquals("verify correct transaction hash", "some hash",
        signedTransaction.getTransactionHash());
    assertEquals("verify transaction has one input", 1, signedTransaction.getInputs().size());
    TransactionInput input = signedTransaction.getInputs().get(0);
    assertTrue("verify correct transaction input amount", input.getAmount()
        .compareTo(new BigDecimal(requestInput.getAmount())) == 0);
    assertEquals("verify correct transaction input pub key", requestInput.getPublicKey(), 
        input.getPublicKey());
    assertEquals("verify correct transaction input seq number", requestInput.getSequenceNumber(), 
        input.getSequenceNumber());
    assertNotNull("verify transaction input has timestamp", input.getTimestamp());
    assertEquals("verify correct transaction input signature", "x0x0x0", input.getSignature());
    assertEquals("verify transaction has correct number of outputs", 3, signedTransaction.getOutputs().size());
    TransactionOutput output = signedTransaction.getOutputs().get(0);
    assertNotNull("verify transaction output has amount", output.getAmount());
    assertNotNull("verify transaction output has pub key", output.getPublicKey());
    assertNotNull("verify transaction output has seq number", output.getSequenceNumber());
    assertNotNull("verify transaction output has timestamp", output.getTimestamp());
  }
  
  private TransactionStatementDto createStatement(long sequenceNum) {
    TransactionStatementDto statement = new TransactionStatementDto();
    statement.setAmount(RandomUtils.nextDouble(1, 99));
    statement.setPublicKey(UUID.randomUUID().toString());
    statement.setSequenceNumber(sequenceNum);
    statement.setTimestamp(new Date().getTime());
    return statement;
  }
  
  @Test(expected=AssessmentFailedException.class)
  public void testCreateSignedTransaction_withNoOutputBalanceFound_shouldThrowException() {   
    when(validator.validate(any())).thenThrow(AssessmentFailedException.class);
    
    TransactionDto request = new TransactionDto();
    TransactionStatementDto input  = new TransactionStatementDto();
    input.setAmount(999d);
    input.setPublicKey("stolenKey");
    input.setSequenceNumber(1l);
    input.setTimestamp(3000l);
    request.setInputs(ImmutableList.of(input));
    request.setOutputs(ImmutableList.of(createStatement(1),createStatement(2),createStatement(3)));
    
    manager.createSignedTransaction(request);
  }
  
  @Test
  public void testCreateSignedTransaction_withNoOutputBalanceFoundButBeingNodeRewardTransaction_shouldWork() {  
    TransactionDto request = new TransactionDto();
    TransactionStatementDto input  = new TransactionStatementDto();
    input.setAmount(100d);
    input.setPublicKey("masta-key");
    input.setSequenceNumber(1l);
    input.setTimestamp(3000l);
    request.setInputs(ImmutableList.of(input));
    request.setOutputs(ImmutableList.of(createStatement(1),createStatement(2),createStatement(3)));
    
    Transaction signedTransaction = manager.createSignedTransaction(request);
    // assert
    assertNotNull("verify signed transaction is not null", signedTransaction);
  }
  
  @Test
  public void testCreateSignedTransaction_withValidRequestSentTwiceWithShuffledOutputList_shouldResultEqual() {  
    when(transactionUtils.sign(any(), any())).thenAnswer(new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        byte[] toSign = (byte[])invocation.getArguments()[0];
        return new String(toSign, StandardCharsets.UTF_8);
      }});
    
    TransactionDto request = new TransactionDto();
    TransactionStatementDto input  = new TransactionStatementDto();
    input.setAmount(100d);
    input.setPublicKey("masta-key");
    input.setSequenceNumber(1l);
    input.setTimestamp(3000l);
    request.setInputs(ImmutableList.of(input));
    var outputs = new ArrayList<TransactionStatementDto>();
    outputs.addAll(ImmutableList.of(createStatement(1),createStatement(2),createStatement(3)));
    request.setOutputs(outputs);
    
    String signature = manager.createSignedTransaction(request).getInputs().get(0).getSignature();
    String sameSignature = manager.createSignedTransaction(request).getInputs().get(0).getSignature();
    assertEquals("verify that signature is the equal also with non-shuffled outputs", signature, sameSignature);
    Collections.shuffle(request.getOutputs());
    String shuffledSignature = manager.createSignedTransaction(request).getInputs().get(0).getSignature();
    
    // assert
    assertEquals("verify that signature is the equal also with shuffled outputs", signature, shuffledSignature);
  }
  
  @Test(expected = CantConfigureSigningAlgorithmException.class)
  public void testCreateSignedTransaction_withSigningFails_shouldThrowException() {
    when(transactionUtils.sign(any(), any())).thenThrow(CantConfigureSigningAlgorithmException.class);
    // mock
    when(transactionUtils.generateHash(any())).thenReturn("some hash");
    when(transactionUtils.sign(any(), any())).thenReturn("x0x0x0");
    StoredTransactionOutput requestOutput = new StoredTransactionOutput();
    requestOutput.setAmount(new BigDecimal("2000.2"));
    requestOutput.setPublicKey("masterkey");
    requestOutput.setSequenceNumber(2l);
    requestOutput.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(requestOutput, "bestHashEver"));
    
    TransactionDto request = new TransactionDto();
    TransactionStatementDto input  = new TransactionStatementDto();
    input.setAmount(999d);
    input.setPublicKey("stolenKey");
    input.setSequenceNumber(1l);
    input.setTimestamp(3000l);
    request.setInputs(ImmutableList.of(input));
    request.setOutputs(ImmutableList.of(createStatement(1),createStatement(2),createStatement(3)));
    
    manager.createSignedTransaction(request);
  }
  
  @Test(expected = HashingException.class)
  public void testCreateSignedTransaction_withHashingFails_shouldThrowException() {
    when(transactionUtils.generateHash(any())).thenThrow(HashingException.class);
    // mock
    when(transactionUtils.generateHash(any())).thenReturn("some hash");
    when(transactionUtils.sign(any(), any())).thenReturn("x0x0x0");
    StoredTransactionOutput requestOutput = new StoredTransactionOutput();
    requestOutput.setAmount(new BigDecimal("2000.2"));
    requestOutput.setPublicKey("masterkey");
    requestOutput.setSequenceNumber(2l);
    requestOutput.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(requestOutput, "bestHashEver"));
    
    TransactionDto request = new TransactionDto();
    TransactionStatementDto input  = new TransactionStatementDto();
    input.setAmount(999d);
    input.setPublicKey("stolenKey");
    input.setSequenceNumber(1l);
    input.setTimestamp(3000l);
    request.setInputs(ImmutableList.of(input));
    request.setOutputs(ImmutableList.of(createStatement(1),createStatement(2),createStatement(3)));
    
    manager.createSignedTransaction(request);
  }
    
  
  @Test
  public void testUpdateTransactionStatuses_withSomeExistingSomeNot_shouldUpdateExistingOnesCorrectly() {
    List<StoredPoolTransaction> storedTx = new ArrayList<>();
    storedTx.addAll(fakePoolTransactions);
    storedTx.forEach(tx -> tx.setStatus(TransactionStatus.RAW));
    poolDao.saveAll(storedTx);
    long before = poolDao.findAll().stream()
        .filter(tx -> TransactionStatus.RAW.equals(tx.getStatus())).count();
    assertEquals("verify all are unmodified before", 10l, before);
    
    List<Transaction> freshTranasctions = storedTx.stream()
        .map(pooledTx -> mapper.map(pooledTx, Transaction.class)).collect(Collectors.toList()).subList(2, 8);
    freshTranasctions.get(2).setTransactionHash("nonExistante1");
    freshTranasctions.get(4).setTransactionHash("nonExistante2");
    
    manager.updateTransactionStatuses(freshTranasctions, TransactionStatus.EMBEDDED_IN_BLOCK);
    
    List<StoredPoolTransaction> allTx = poolDao.findAll();
    long modifiedTransactions = allTx.stream()
        .filter(tx -> TransactionStatus.EMBEDDED_IN_BLOCK.equals(tx.getStatus())).count();
    long unmodifiedTransactions = allTx.stream()
        .filter(tx -> TransactionStatus.RAW.equals(tx.getStatus())).count();
    assertEquals("verify correct amount of transactions was updated to EMBEDDED_IN_BLOCK", 4l, modifiedTransactions);
    assertEquals("verify correct rest of the transactions was untouched", 6l, unmodifiedTransactions);
  }
  
  @Test
  public void testUpdateTransactionStatuses_withNoneExisting_shouldDoNothing() {
    List<StoredPoolTransaction> storedTx = new ArrayList<>();
    storedTx.addAll(fakePoolTransactions);
    storedTx.forEach(tx -> tx.setStatus(TransactionStatus.RAW));
    poolDao.saveAll(storedTx);
    
    List<Transaction> freshTranasctions = storedTx.stream()
        .map(pooledTx -> mapper.map(pooledTx, Transaction.class)).collect(Collectors.toList()).subList(2, 8);
    freshTranasctions.forEach(tx -> tx.setTransactionHash("muiPicante" + UUID.randomUUID().toString()));
    
    manager.updateTransactionStatuses(freshTranasctions, TransactionStatus.EMBEDDED_IN_BLOCK);
    
    List<StoredPoolTransaction> allTx = poolDao.findAll();
    long modifiedTransactions = allTx.stream()
        .filter(tx -> TransactionStatus.EMBEDDED_IN_BLOCK.equals(tx.getStatus())).count();
    long unmodifiedTransactions = allTx.stream()
        .filter(tx -> TransactionStatus.RAW.equals(tx.getStatus())).count();
    assertEquals("verify correct amount of transactions was updated to EMBEDDED_IN_BLOCK", 0l, modifiedTransactions);
    assertEquals("verify correct rest of the transactions was untouched", 10l, unmodifiedTransactions);
  }
  
  
  @Test
  public void testStoreTransaction_withValidTransaction_shouldStore() {
    Transaction transaction = TestDataFactory.createValidTransaction("ex1", "ex2", "ex3", "in1");
    when(validator.validate(any())).thenReturn(Assessment.build().valid(true));
    
    TransactionIdDto txId = manager.storeTransaction(transaction);
    
    assertNotNull("verify returned transactionId entity is not null", txId);
    assertEquals("verify transactionId contains hash", transaction.getTransactionHash(), txId.getId());
    
    Optional<StoredPoolTransaction> storedTx = poolDao.findAll().stream().findFirst();
    assertTrue("verify stored transaction exists", storedTx.isPresent());
    assertEquals("verify correct status", TransactionStatus.RAW, storedTx.get().getStatus());
    assertNotNull("verify it has a createdAt date", storedTx.get().getCreatedAt());
    assertEquals("verify transaction hash", transaction.getTransactionHash(), storedTx.get().getTransactionHash());
    assertEquals("verify transaction input count", transaction.getInputs().size(), storedTx.get().getInputs().size());
    TransactionInput input = transaction.getInputs().get(0);
    StoredTransactionInput storedInput = storedTx.get().getInputs().get(0);
    assertEquals("verify transaction input amount", input.getAmount(), storedInput.getAmount());
    assertEquals("verify transaction input pub key", input.getPublicKey(), storedInput.getPublicKey());
    assertEquals("verify transaction input seq number", input.getSequenceNumber(), storedInput.getSequenceNumber());
    assertEquals("verify transaction input signature", input.getSignature(), storedInput.getSignature());
    assertEquals("verify transaction input timestamp", input.getTimestamp(), storedInput.getTimestamp());
    assertEquals("verify transaction output count", transaction.getOutputs().size(), storedTx.get().getOutputs().size());
    TransactionOutput output = transaction.getOutputs().get(0);
    StoredTransactionOutput storedOutput = storedTx.get().getOutputs().get(0);
    assertEquals("verify transaction output amount", output.getAmount(), storedOutput.getAmount());
    assertEquals("verify transaction output pub key", output.getPublicKey(), storedOutput.getPublicKey());
    assertEquals("verify transaction output seq number", output.getSequenceNumber(), storedOutput.getSequenceNumber());
    assertEquals("verify transaction output timestamp", output.getTimestamp(), storedOutput.getTimestamp());
  }
  
  
  @Test(expected=AssessmentFailedException.class)
  public void testStoreTransaction_withInvalidTransaction_shouldThrowException() {
    Transaction transaction = TestDataFactory.createValidTransaction("ex1", "ex2", "ex3", "in1");
    when(validator.validate(any())).thenReturn(Assessment.build().valid(false).reason("cause, reasons..."));
    
    manager.storeTransaction(transaction);
  }
  
  @Test(expected=TransactionAlreadyClearedException.class)
  public void testStoreTransaction_withTransactionExistsAlready_shouldThrowException() {
    Transaction transaction = TestDataFactory.createValidTransaction("ex1", "ex2", "ex3", "in1");
    when(validator.validate(any())).thenReturn(Assessment.build().valid(true));
    
    manager.storeTransaction(transaction);
    manager.storeTransaction(transaction);
  }
  
  @Test
  public void testGetStatusOfTransaction_withTxInPool_shouldReturnCorrect() {
    var transactions = createFakePooledTransactions();
    transactions.get(0).setTransactionHash("1234");
    poolDao.saveAll(transactions);
    
    TransactionStatusDto status = manager.getStatusOfTransaction("1234");
    assertNotNull("verify returned status is not null", status);
    assertEquals("verify correct transaction status", 
        transactions.get(0).getStatus().name(), status.getStatus());
    assertEquals("verify correct transaction status message", 
        transactions.get(0).getStatus().description(), status.getStatusMessage());
  }
  
  @Test
  public void testGetStatusOfTransaction_withTxInBlockchain_shouldReturnCorrect() {
    poolDao.saveAll(fakePoolTransactions);
    blockDao.saveAll(fakeBlocks(new StoredTransactionOutput(),"1234"));
    
    TransactionStatusDto status = manager.getStatusOfTransaction("1234");
    assertNotNull("verify returned status is not null", status);
    assertEquals("verify correct transaction status", 
        TransactionStatus.SIX_BLOCKS_UNDER.name(), status.getStatus());
    assertEquals("verify correct transaction status message", 
        TransactionStatus.SIX_BLOCKS_UNDER.description(), status.getStatusMessage());
  }
  
  @Test(expected = TransactionNotFoundException.class)
  public void testGetStatusOfTransaction_withTransactionNowhere_shouldThrowException() {
    poolDao.saveAll(fakePoolTransactions);
    blockDao.saveAll(fakeBlocks(null, null));
    
    manager.getStatusOfTransaction("1234");
  }
  
  
  @Test
  public void testgetTransactionsFromPublicKey_withTransactionsAndBlockTxs_shouldReturnCorrect() {
    final String pubKey = "monsterOfDesaster";
    var transactions = createFakePooledTransactions();
    transactions.get(0).setTransactionHash("1234");
    transactions.get(0).getOutputs().get(0).setPublicKey(pubKey);
    transactions.get(1).setTransactionHash("234");
    transactions.get(1).getInputs().get(0).setPublicKey(pubKey);
    transactions.get(2).setTransactionHash("1234");
    transactions.get(2).getOutputs().get(0).setPublicKey(pubKey);
    poolDao.saveAll(transactions);
    var blocks = fakeBlocks(null, null);
    blocks.get(0).getTransactions().get(0).setTransactionHash("1234");
    blocks.get(0).getTransactions().get(0).getInputs().get(0).setPublicKey(pubKey);
    blocks.get(1).getTransactions().get(0).setTransactionHash("456");
    blocks.get(1).getTransactions().get(0).getInputs().get(0).setPublicKey(pubKey);
    blocks.get(1).getTransactions().get(1).setTransactionHash("789");
    blocks.get(1).getTransactions().get(1).getOutputs().get(0).setPublicKey(pubKey);
    blockDao.saveAll(blocks);
    
    var pubKeyTxs = manager.getTransactionsFromPublicKey(pubKey);
    assertNotNull("verify returned transactions are not null", pubKeyTxs);
    assertEquals("verify correct transaction count for pub key", 4, pubKeyTxs.size());
    assertNotNull("verify transaction id is not null", pubKeyTxs.get(0).getId());
    assertNotNull("verify transaction outputs is not null", pubKeyTxs.get(0).getOutputs());
    assertNotNull("verify transaction output amount is not null", 
        pubKeyTxs.get(0).getOutputs().get(0).getAmount());
    assertNotNull("verify transaction output pubKey is not null", 
        pubKeyTxs.get(0).getOutputs().get(0).getPublicKey());
    assertNotNull("verify transaction output sequence number is not null", 
        pubKeyTxs.get(0).getOutputs().get(0).getSequenceNumber());
    assertNotNull("verify transaction output timestamp is not null", 
        pubKeyTxs.get(0).getOutputs().get(0).getTimestamp());
    assertNotNull("verify transaction inputs is not null", pubKeyTxs.get(0).getInputs());
    assertNotNull("verify transaction output amount is not null", 
        pubKeyTxs.get(0).getInputs().get(0).getAmount());
    assertNotNull("verify transaction output pubKey is not null", 
        pubKeyTxs.get(0).getInputs().get(0).getPublicKey());
    assertNotNull("verify transaction output sequence number is not null", 
        pubKeyTxs.get(0).getInputs().get(0).getSequenceNumber());
    assertNotNull("verify transaction output timestamp is not null", 
        pubKeyTxs.get(0).getInputs().get(0).getTimestamp());
    assertNotNull("verify transaction output signature is not null", 
        pubKeyTxs.get(0).getInputs().get(0).getSignature());
  }
  
  @Test
  public void testgetTransactionsFromPublicKey_withNoTransactionsDone_shouldReturnCorrect() {
    final String pubKey = "masterOfDesaster";
    
    var pubKeyTxs = manager.getTransactionsFromPublicKey(pubKey);
    assertNotNull("verify returned transactions are not null", pubKeyTxs);
    assertEquals("verify correct transaction count for pub key", 0, pubKeyTxs.size());
  }
  
  
  
  private List<StoredPoolTransaction> createFakePooledTransactions() {
    List<StoredPoolTransaction> poolTransactions = new ArrayList<>();
    for(int i=0; i < 10; i++) {
      TransactionStatus status = TransactionStatus.RAW;
      if(i%3 == 0) {
        status = TransactionStatus.SIX_BLOCKS_UNDER;
      } else if (i%4 == 0) {
        status = TransactionStatus.EMBEDDED_IN_BLOCK;
      }
      poolTransactions.add(fakePooledTransaction(1000 -i, status));
    }
    return poolTransactions;
  }
  
  private StoredPoolTransaction fakePooledTransaction(long createdAt, TransactionStatus status) {
    StoredPoolTransaction tr = new StoredPoolTransaction();
    tr.setCreatedAt(new Date(createdAt));
    tr.setId(UUID.randomUUID().toString());
    tr.setOutputs(ImmutableList.of(createRandomTransactionOutputWith(1),createRandomTransactionOutputWith(2),
        createRandomTransactionOutputWith(3),createRandomTransactionOutputWith(4)));
    tr.setInputs(ImmutableList.of(createRandomTransactionInputWith(1),createRandomTransactionInputWith(2),
        createRandomTransactionInputWith(3)));
    tr.setStatus(status);
    tr.setTransactionHash(Long.toString(createdAt));
    return tr;
  }
  
  private List<StoredBlock> fakeBlocks(StoredTransactionOutput importantOutput, String importantHash) {
    List<StoredBlock> blocks = new ArrayList<>();
    StoredBlock block1 = new StoredBlock();
    block1.setPosition(1L);
    block1.setTransactions(createFakeTransactions(null,null));
    blocks.add(block1);
    StoredBlock block2 = new StoredBlock();
    block2.setPosition(2L);
    block2.setTransactions(createFakeTransactions(null,null));
    blocks.add(block2);
    StoredBlock block3 = new StoredBlock();
    block3.setPosition(3L);
    block3.setTransactions(createFakeTransactions(null,null));
    blocks.add(block3);
    StoredBlock block4 = new StoredBlock();
    block4.setPosition(4L);
    block4.setHash("I'm the one");
    block4.setTransactions(createFakeTransactions(importantOutput, importantHash));
    blocks.add(block4);
    StoredBlock block5 = new StoredBlock();
    block5.setPosition(5L);
    block5.setTransactions(createFakeTransactions(null,null));
    blocks.add(block5);
    StoredBlock block6 = new StoredBlock();
    block6.setPosition(6L);
    block6.setTransactions(createFakeTransactions(null,null));
    blocks.add(block6);
    return blocks;
  }
  
  
  private List<StoredTransaction> createFakeTransactions(StoredTransactionOutput importantOutput, String importantHash) {
    List<StoredTransaction> transactions = new ArrayList<>();
    transactions.add(createRandomTransactionWith(null,null,null));
    transactions.add(createRandomTransactionWith(null,null,null));
    transactions.add(createRandomTransactionWith(null,null,null));
    transactions.add(createRandomTransactionWith(null,null,null));
    if(importantOutput != null && importantHash != null) {
      transactions.add(createRandomTransactionWith(importantHash,importantOutput,null));
    }
    transactions.add(createRandomTransactionWith(null,null,null));
    transactions.add(createRandomTransactionWith(null,null,null));
    transactions.add(createRandomTransactionWith(null,null,null));
    transactions.add(createRandomTransactionWith(null,null,null));
    return transactions;
  }
}
