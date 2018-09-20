package com.flockinger.groschn.blockchain.blockworks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockStorageServiceImpl;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.exception.validation.BlockValidationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.validation.impl.BlockValidator;
import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes = {BlockchainRepository.class, BlockStorageServiceImpl.class})
public class BlockStorageServiceTest extends BaseDbTest {

  @Autowired
  private BlockStorageService service;
  @Autowired
  private BlockchainRepository dao;
  @Autowired
  private ModelMapper mapper;
  
  @MockBean
  private BlockValidator validator;
  @MockBean
  private TransactionManager transactionManager;
  
  
  @After
  public void teardown() {
    dao.deleteAll();
  }
  
  @Test
  public void testInit_shouldHaveGenesisBlockCreated() {
    ((BlockStorageServiceImpl)service).initBlockchain();
    Optional<StoredBlock> genesis = dao.findAll().stream().findFirst();
    assertTrue("verify genesis block was first inserted", genesis.isPresent());
    assertEquals("verify genesis block hash", Block.GENESIS_BLOCK().getHash(), genesis.get().getHash());
  }
  
  @Test
  public void testStoreInBlockchain_withValidBlock_shouldStoreCorrectly() {
    Block freshBlock = TestDataFactory.getFakeBlock();
    when(validator.validate(any())).thenReturn(TestDataFactory.fakeAssessment(true));
    
    StoredBlock savedBlock = service.saveInBlockchain(freshBlock);
    assertNotNull("verify that returned block is not null", savedBlock);
    assertNotNull("verify that returned block has an ID", savedBlock.getId());

    Optional<StoredBlock> storedBlock = dao.findById(savedBlock.getId());
    assertTrue("verify that stored block exists", storedBlock.isPresent());
    assertEquals("verify correct block consent difficulty", freshBlock.getConsent().getDifficulty(), storedBlock.get().getConsent().getDifficulty());
    assertEquals("verify correct block consent millisecondsspent", freshBlock.getConsent().getMilliSecondsSpentMining(), storedBlock.get().getConsent().getMilliSecondsSpentMining());
    assertEquals("verify correct block consent nonce", freshBlock.getConsent().getNonce(), storedBlock.get().getConsent().getNonce());
    assertEquals("verify correct block consent timestamp", freshBlock.getConsent().getTimestamp(), storedBlock.get().getConsent().getTimestamp());
    assertEquals("verify correct block consent type", freshBlock.getConsent().getType(), storedBlock.get().getConsent().getType());
    assertEquals("verify correct block hash ", freshBlock.getHash(), storedBlock.get().getHash());
    assertEquals("verify correct block last hash",freshBlock.getLastHash(), storedBlock.get().getLastHash());
    assertEquals("verify correct block position", freshBlock.getPosition(), storedBlock.get().getPosition());
    assertEquals("verify correct block timestamp", freshBlock.getTimestamp(), storedBlock.get().getTimestamp());
    assertEquals("verify correct block transactions merkle root", freshBlock.getTransactionMerkleRoot(), storedBlock.get().getTransactionMerkleRoot());
    assertEquals("verify correct block transaction size", freshBlock.getTransactions().size(), storedBlock.get().getTransactions().size());
    Transaction firstTransaction = freshBlock.getTransactions().get(0);
    StoredTransaction firstUncompressedTr = storedBlock.get().getTransactions().get(0);
    assertEquals("verify correct block first transaction inputs size", firstTransaction.getInputs().size()
        , firstUncompressedTr.getInputs().size());
    assertEquals("verify correct block first transaction input amount", firstTransaction.getInputs().get(0).getAmount(), 
        firstUncompressedTr.getInputs().get(0).getAmount());
    assertEquals("verify correct block first transaction input pub key", firstTransaction.getInputs().get(0).getPublicKey(), 
        firstUncompressedTr.getInputs().get(0).getPublicKey());
    assertEquals("verify correct block first transaction input squence number", firstTransaction.getInputs().get(0).getSequenceNumber(), 
        firstUncompressedTr.getInputs().get(0).getSequenceNumber());
    assertEquals("verify correct block first transaction input signature", firstTransaction.getInputs().get(0).getSignature(), 
        firstUncompressedTr.getInputs().get(0).getSignature());
    assertEquals("verify correct block first transaction input timestamp", firstTransaction.getInputs().get(0).getTimestamp(), 
        firstUncompressedTr.getInputs().get(0).getTimestamp());
    assertEquals("verify correct block first transaction output amount", firstTransaction.getOutputs().get(0).getAmount(), 
        firstUncompressedTr.getOutputs().get(0).getAmount());
    assertEquals("verify correct block first transaction output pub key", firstTransaction.getOutputs().get(0).getPublicKey(), 
        firstUncompressedTr.getOutputs().get(0).getPublicKey());
    assertEquals("verify correct block first transaction output squence number", firstTransaction.getOutputs().get(0).getSequenceNumber(), 
        firstUncompressedTr.getOutputs().get(0).getSequenceNumber());
    assertEquals("verify correct block first transaction output timestamp", firstTransaction.getOutputs().get(0).getTimestamp(), 
        firstUncompressedTr.getOutputs().get(0).getTimestamp());
    assertEquals("verify correct block first transaction locktime", firstTransaction.getLockTime().longValue(), 
        firstUncompressedTr.getLockTime().getTime());
    assertEquals("verify correct block ", freshBlock.getVersion(), storedBlock.get().getVersion());
    
    ArgumentCaptor<TransactionStatus> statusCaptor = ArgumentCaptor.forClass(TransactionStatus.class);
    verify(transactionManager).updateTransactionStatuses(anyList(), statusCaptor.capture());
    assertEquals("verify transactions will be updated with correct status", TransactionStatus.EMBEDDED_IN_BLOCK, statusCaptor.getValue());
  }
  
  @Test(expected=BlockValidationException.class)
  public void testStoreInBlockchain_withInvalidBlock_shouldThrowException() {
    Block freshBlock = TestDataFactory.getFakeBlock();
    when(validator.validate(any())).thenReturn(TestDataFactory.fakeAssessment(false));
    service.saveInBlockchain(freshBlock);
  }
  
  @Test
  public void testgetLatestBlockPosition_shouldReturnCorrect() {
    when(validator.validate(any())).thenReturn(TestDataFactory.fakeAssessment(true));
    service.saveInBlockchain(TestDataFactory.getFakeBlock());
    
    Block lastBlock = service.getLatestBlock();
    assertNotNull("verify latest block position is not null", lastBlock);
    assertEquals("verify latest block position is correct", 97l, lastBlock.getPosition().longValue());
  }
  
  @Test
  public void testGetLatestProofOfWorkBlock_filledChain_shouldReturnCorrect() {
    dao.saveAll(fakeBlocks(23l,50l,54l,98l,101l));
    
    Block lastBlock = service.getLatestProofOfWorkBlock();
    assertNotNull("verify last block is not null", lastBlock);
    assertEquals("verify last block has correct position", 98l, 
        lastBlock.getPosition().longValue());
    assertEquals("verify last block is of right consensus type", 
        ConsensusType.PROOF_OF_WORK, lastBlock.getConsent().getType());
  }
  
  @Test
  public void testGetLatestProofOfWorkBlock_withEmptyChain_shouldReturnGenesisBlock() {
    ((BlockStorageServiceImpl)service).initBlockchain();
    
    Block lastBlock = service.getLatestProofOfWorkBlock();
    assertNotNull("verify last block is not null", lastBlock);
    assertEquals("verify last block has correct position", 1l, 
        lastBlock.getPosition().longValue());
    assertEquals("verify last block is of right consensus type", 
        ConsensusType.PROOF_OF_WORK, lastBlock.getConsent().getType());
  }
  
  @Test
  public void testFindBlocks_withValidPositionAndQuandity_shouldWork() {
    dao.saveAll(fakeBlocks(3l,2l,1l,4l,5l));
    
    List<Block> blocks = service.findBlocks(2, 3);
    assertNotNull("verify received blocks are not null", blocks);
    assertEquals("verify correct response amount", 3, blocks.size());
    assertTrue("verify correct blocks are received", blocks.stream().map(Block::getPosition)
        .collect(Collectors.toList()).containsAll(ImmutableList.of(2l,3l,4l)));
    
    List<Block> blocks3 = service.findBlocks(1, 2);
    assertNotNull("verify received blocks are not null", blocks3);
    assertEquals("verify correct response amount", 2, blocks3.size());
    assertTrue("verify correct blocks are received", blocks3.stream().map(Block::getPosition)
        .collect(Collectors.toList()).containsAll(ImmutableList.of(1l,2l)));
    
    List<Block> blocks2 = service.findBlocks(3, 5);
    assertNotNull("verify received blocks are not null", blocks2);
    assertEquals("verify correct response amount", 3, blocks2.size());
    assertTrue("verify correct blocks are received", blocks2.stream().map(Block::getPosition)
        .collect(Collectors.toList()).containsAll(ImmutableList.of(3l,4l,5l)));
  }
  
  @Test
  public void testFindBlocks_withZeroQuantity_shouldWork() {
    dao.saveAll(fakeBlocks(3l,2l,1l,4l,5l));
    
    List<Block> blocks = service.findBlocks(2, 0);
    assertNotNull("verify received blocks are not null", blocks);
    assertEquals("verify correct response amount", 0, blocks.size());
  }
  
  @Test
  public void testFindBlocks_withNegativeStartingPoint_shouldWork() {
    dao.saveAll(fakeBlocks(3l,2l,1l,4l,5l));
    
    List<Block> blocks = service.findBlocks(-3, 3);
    assertNotNull("verify received blocks are not null", blocks);
    assertEquals("verify correct response amount", 3, blocks.size());
    assertTrue("verify correct blocks are received", blocks.stream().map(Block::getPosition)
        .collect(Collectors.toList()).containsAll(ImmutableList.of(1l,2l,3l)));
  }
  
  @Test
  public void testFindBlocks_withBothNegative_shouldWork() {
    dao.saveAll(fakeBlocks(3l,2l,1l,4l,5l));
    
    List<Block> blocks = service.findBlocks(-3, -3);
    assertNotNull("verify received blocks are not null", blocks);
    assertEquals("verify correct response amount", 0, blocks.size());
  }
  
  @Test
  public void testRemoveBlocks_withMiddlePosition_shouldRemoveCorrect() {
    dao.saveAll(fakeBlocks(3l,2l,1l,4l,5l));
    
    service.removeBlocks(3);
    
    var existingBlocks = dao.findAll();
    assertEquals("verify that enough blocks still exist", 2, existingBlocks.size());
    assertTrue("verify that the first block still exists", existingBlocks.stream().anyMatch(b -> b.getPosition() == 1));
    assertTrue("verify that the 2nd block still exists", existingBlocks.stream().anyMatch(b -> b.getPosition() == 2));
  }
  
  @Test
  public void testRemoveBlocks_withZeroPosition_shouldNotRemoveGenesisBlock() {
    dao.saveAll(fakeBlocks(5l,3l,2l,5l,6l));
    
    service.removeBlocks(1);
    
    var existingBlocks = dao.findAll();
    assertEquals("verify that enough blocks still exist", 1, existingBlocks.size());
    assertTrue("verify that the first block still exists", existingBlocks.stream().anyMatch(b -> b.getPosition() == 1));
  }
  
  @Test
  public void testRemoveBlocks_withTooHighPosition_shouldDoNothing() {
    dao.saveAll(fakeBlocks(3l,2l,1l,4l,5l));
    
    service.removeBlocks(6);
    
    var existingBlocks = dao.findAll();
    assertEquals("verify that enough blocks still exist", 5, existingBlocks.size());
  }
  
  
  private List<StoredBlock> fakeBlocks(long pos1, long pos2, long pos3, long pos4, long pos5) {
    Block block1 = TestDataFactory.getFakeBlock();
    block1.setConsent(fakeConsent(ConsensusType.PROOF_OF_WORK));
    block1.setPosition(pos1);
    Block block2 = TestDataFactory.getFakeBlock();
    block2.setConsent(fakeConsent(ConsensusType.PROOF_OF_MAJORITY));
    block2.setPosition(pos2);
    Block block3 = TestDataFactory.getFakeBlock();
    block3.setConsent(fakeConsent(ConsensusType.PROOF_OF_WORK));
    block3.setPosition(pos3);
    Block block4 = TestDataFactory.getFakeBlock();
    block4.setConsent(fakeConsent(ConsensusType.PROOF_OF_WORK));
    block4.setPosition(pos4);
    Block block7 = TestDataFactory.getFakeBlock();
    block7.setConsent(fakeConsent(ConsensusType.PROOF_OF_MAJORITY));
    block7.setPosition(pos5);
    return ImmutableList.of(map(block1), map(block7), map(block3), map(block4), map(block2));
  }
  
  private Consent fakeConsent(ConsensusType type) {
    Consent consent = new Consent();
    consent.setType(type);
    return consent;
  }
  
  private StoredBlock map(Block block) {
    return mapper.map(block, StoredBlock.class);
  }
  
}
