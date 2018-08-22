package com.flockinger.groschn.blockchain.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.model.TransactionPointCut;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionInput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionOutput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionPointCut;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionManagerImpl;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionPoolListener;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.messaging.distribution.DistributedCollectionBuilder;
import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes = {TransactionManagerImpl.class, TransactionPoolRepository.class, 
    BlockchainRepository.class, CompressionUtils.class})
public class TransactionManagerTest extends BaseDbTest {

  @Autowired
  private TransactionManager manager;
  
  @MockBean
  private DistributedCollectionBuilder distributedCollectionBuilder;
  @MockBean
  private TransactionPoolListener transactionListener;
  @MockBean(name="ECDSA_Signer")
  private Signer signer;
  
  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private TransactionPoolRepository poolDao;
  @Autowired
  private CompressionUtils compressor;
  
  private List<StoredPoolTransaction> fakePoolTransactions = createFakePooledTransactions();
  
  @Before
  public void setup() {
    when(distributedCollectionBuilder.createSetWithListener(any(), anyString())).thenReturn(null);
    poolDao.deleteAll();
    blockDao.deleteAll();
  }
  
  @Test
  public void testFindTransactionFromPointCut_withExistingPointCut_shouldFindAndReturnCorrect() {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal("2000.2"));
    output.setPublicKey("masterkey");
    output.setSequenceNumber(2l);
    output.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(output, "bestHashEver"));
    TransactionPointCut pointcut = new TransactionPointCut();
    pointcut.setSequenceNumber(2l);
    pointcut.setTransactionHash("bestHashEver");
    
    Optional<TransactionOutput> pointOutput = manager.findTransactionFromPointCut(pointcut);
    
    assertTrue("verify that point cut output is present/found", pointOutput.isPresent());
    assertTrue("verify correct output amount", pointOutput.get().getAmount().compareTo(output.getAmount()) == 0);
    assertEquals("verify correct output pub key", output.getPublicKey(), pointOutput.get().getPublicKey());
    assertEquals("verify correct output timestamp", output.getTimestamp(),pointOutput.get().getTimestamp());
    assertEquals("verify correct output sequence number", 2l, pointOutput.get().getSequenceNumber().longValue());
  }
  
  @Test
  public void testFindTransactionFromPointCut_withNonExistingTransactionHash_shouldReturnEmpty() {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal("2000.2"));
    output.setPublicKey("masterkey");
    output.setSequenceNumber(2l);
    output.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(output, "bestHashEver"));
    TransactionPointCut pointcut = new TransactionPointCut();
    pointcut.setSequenceNumber(2l);
    pointcut.setTransactionHash("bestHaschEver");
    
    Optional<TransactionOutput> pointOutput = manager.findTransactionFromPointCut(pointcut);
    
    assertFalse("verify that point cut output is not present", pointOutput.isPresent());
  }
  
  @Test
  public void testFindTransactionFromPointCut_withNonExistingSequenceNumber_shouldReturnEmpty() {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal("2000.2"));
    output.setPublicKey("masterkey");
    output.setSequenceNumber(2l);
    output.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(output, "bestHashEver"));
    TransactionPointCut pointcut = new TransactionPointCut();
    pointcut.setSequenceNumber(22l);
    pointcut.setTransactionHash("bestHashEver");
    
    Optional<TransactionOutput> pointOutput = manager.findTransactionFromPointCut(pointcut);
    
    assertFalse("verify that point cut output is not present", pointOutput.isPresent());
  }
  
  
  @Test
  public void testFindTransactionFromPointCut_withSequenceNumberNull_shouldReturnEmpty() {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal("2000.2"));
    output.setPublicKey("masterkey");
    output.setSequenceNumber(2l);
    output.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(output, "bestHashEver"));
    TransactionPointCut pointcut = new TransactionPointCut();
    pointcut.setSequenceNumber(null);
    pointcut.setTransactionHash("bestHashEver");
    
    Optional<TransactionOutput> pointOutput = manager.findTransactionFromPointCut(pointcut);
    
    assertFalse("verify that point cut output is not present", pointOutput.isPresent());
  }
  
  @Test
  public void testFindTransactionFromPointCut_withTransactionHashNull_shouldReturnEmpty() {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal("2000.2"));
    output.setPublicKey("masterkey");
    output.setSequenceNumber(2l);
    output.setTimestamp(2000l);
    blockDao.saveAll(fakeBlocks(output, "bestHashEver"));
    TransactionPointCut pointcut = new TransactionPointCut();
    pointcut.setSequenceNumber(2l);
    pointcut.setTransactionHash(null);
    
    Optional<TransactionOutput> pointOutput = manager.findTransactionFromPointCut(pointcut);
    
    assertFalse("verify that point cut output is not present", pointOutput.isPresent());
  }
  
  
  @Test
  public void testFetchTransactionsFromPool_withSomeByteSize_shouldReturnCorrect() {
    poolDao.saveAll(fakePoolTransactions);
    List<Transaction> transactions = manager.fetchTransactionsFromPool(5000);
    long size = transactions.stream()
        .map(compressor::compress)
        .mapToLong(entity -> entity.getEntity().length)
        .sum();
    assertNotNull("verify returned transactions are not null", transactions);
    assertFalse("verify returned transactions are not empty", transactions.isEmpty());
    assertTrue("verify that returned transactions are smaller than the limit", size < 5000);
  }
  
  @Test
  public void testFetchTransactionsFromPool_withByteSizeZero_shouldReturnEmpty() {
    poolDao.saveAll(fakePoolTransactions);
    
    List<Transaction> transactions = manager.fetchTransactionsFromPool(0);
    assertNotNull("verify returned transactions are not null", transactions);
    assertTrue("verify returned transactions are empty", transactions.isEmpty());
  }
  
  
  /*
  TODO write tests
  
  @Test
 public void test_with_should() {}
   
 Transaction createSignedTransaction(TransactionDto transactionSigningRequest);
  
  
  * */
    
  
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
    tr.setOutputs(ImmutableList.of(createOutput(1),createOutput(2),createOutput(3),createOutput(4)));
    tr.setInputs(ImmutableList.of(createInput(1),createInput(2),createInput(3)));
    tr.setStatus(status);
    tr.setTransactionId(UUID.randomUUID().toString());
    return tr;
  }
  
  private List<StoredBlock> fakeBlocks(StoredTransactionOutput importantOutput, String importantHash) {
    List<StoredBlock> blocks = new ArrayList<>();
    StoredBlock block1 = new StoredBlock(); 
    block1.setTransactions(createFakeTransactions(null,null));
    blocks.add(block1);
    StoredBlock block2 = new StoredBlock(); 
    block2.setTransactions(createFakeTransactions(null,null));
    blocks.add(block2);
    StoredBlock block3 = new StoredBlock(); 
    block3.setTransactions(createFakeTransactions(null,null));
    blocks.add(block3);
    StoredBlock block4 = new StoredBlock(); 
    block4.setHash("I'm the one");
    block4.setTransactions(createFakeTransactions(importantOutput, importantHash));
    blocks.add(block4);
    StoredBlock block5 = new StoredBlock(); 
    block5.setTransactions(createFakeTransactions(null,null));
    blocks.add(block5);
    StoredBlock block6 = new StoredBlock(); 
    block6.setTransactions(createFakeTransactions(null,null));
    blocks.add(block6);
    return blocks;
  }
  
  
  private List<StoredTransaction> createFakeTransactions(StoredTransactionOutput importantOutput, String importantHash) {
    List<StoredTransaction> transactions = new ArrayList<>();
    transactions.add(createFakeTransaction(null,null));
    transactions.add(createFakeTransaction(null,null));
    transactions.add(createFakeTransaction(null,null));
    transactions.add(createFakeTransaction(null,null));
    if(importantOutput != null && importantHash != null) {
      transactions.add(createFakeTransaction(importantOutput, importantHash));
    }
    transactions.add(createFakeTransaction(null,null));
    transactions.add(createFakeTransaction(null,null));
    transactions.add(createFakeTransaction(null,null));
    transactions.add(createFakeTransaction(null,null));
    return transactions;
  }
  
  private StoredTransaction createFakeTransaction(StoredTransactionOutput importantOutput, String importantHash) {
    StoredTransaction transaction = new StoredTransaction();
    if(importantHash != null) {
      transaction.setTransactionHash(importantHash);
    } else {
      transaction.setTransactionHash(UUID.randomUUID().toString());
    }
    if(importantOutput != null) {
      transaction.setOutputs(ImmutableList.of(createOutput(1), importantOutput, createOutput(3),createOutput(4)));
    } else {
      transaction.setOutputs(ImmutableList.of(createOutput(1),createOutput(2),createOutput(3),createOutput(4)));
    }
    return transaction;
  }
  
  private StoredTransactionOutput createOutput(long sequenceNumber) {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal(RandomUtils.nextLong(1, 101)));
    output.setPublicKey(UUID.randomUUID().toString());
    output.setSequenceNumber(sequenceNumber);
    output.setTimestamp(new Date().getTime());
    return output;
  }
  
  private StoredTransactionInput createInput(long sequenceNumber) {
    StoredTransactionInput input = new StoredTransactionInput();
    input.setAmount(new BigDecimal(RandomUtils.nextLong(1, 101)));
    input.setPublicKey(UUID.randomUUID().toString());
    input.setSequenceNumber(sequenceNumber);
    input.setTimestamp(new Date().getTime());
    StoredTransactionPointCut pointcut = new StoredTransactionPointCut();
    pointcut.setSequenceNumber(RandomUtils.nextLong(1, 10));
    pointcut.setTransactionHash(UUID.randomUUID().toString());
    input.setPreviousOutputTransaction(pointcut);
    input.setSignature(UUID.randomUUID().toString());
    return input;
  }
}
