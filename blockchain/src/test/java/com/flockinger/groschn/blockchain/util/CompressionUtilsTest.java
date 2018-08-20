package com.flockinger.groschn.blockchain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CompressionUtils.class})
public class CompressionUtilsTest {

  @Autowired
  private CompressionUtils utils;
  
  @Test
  public void testCompressUncompress_withFullBlock_shouldDoCorrectly() {
    Block fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 1505l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 582l, entity.getEntity().length);
    
    // decompress
    Optional<Block> uncompressedBlock = utils.decompress(entity.getEntity(), entity.getOriginalSize(), Block.class);
    
    assertTrue("verify that uncompressed Block is present", uncompressedBlock.isPresent());
    assertEquals("verify correct block consent difficulty", fakeBlock.getConsent().getDifficulty(), uncompressedBlock.get().getConsent().getDifficulty());
    assertEquals("verify correct block consent millisecondsspent", fakeBlock.getConsent().getMilliSecondsSpentMining(), uncompressedBlock.get().getConsent().getMilliSecondsSpentMining());
    assertEquals("verify correct block consent nonce", fakeBlock.getConsent().getNonce(), uncompressedBlock.get().getConsent().getNonce());
    assertEquals("verify correct block consent timestamp", fakeBlock.getConsent().getTimestamp(), uncompressedBlock.get().getConsent().getTimestamp());
    assertEquals("verify correct block consent type", fakeBlock.getConsent().getType(), uncompressedBlock.get().getConsent().getType());
    assertEquals("verify correct block hash ", fakeBlock.getHash(), uncompressedBlock.get().getHash());
    assertEquals("verify correct block last hash", fakeBlock.getLastHash(), uncompressedBlock.get().getLastHash());
    assertEquals("verify correct block position", fakeBlock.getPosition(), uncompressedBlock.get().getPosition());
    assertEquals("verify correct block timestamp", fakeBlock.getTimestamp(), uncompressedBlock.get().getTimestamp());
    assertEquals("verify correct block transactions merkle root", fakeBlock.getTransactionMerkleRoot(), uncompressedBlock.get().getTransactionMerkleRoot());
    assertEquals("verify correct block transaction size", fakeBlock.getTransactions().size(), uncompressedBlock.get().getTransactions().size());
    Transaction firstTransaction = fakeBlock.getTransactions().get(0);
    Transaction firstUncompressedTr = uncompressedBlock.get().getTransactions().get(0);
    assertEquals("verify correct block first transaction id", firstTransaction.getId()
        , firstUncompressedTr.getId());
    assertEquals("verify correct block first transaction inputs size", firstTransaction.getInputs().size()
        , firstUncompressedTr.getInputs().size());
    assertEquals("verify correct block first transaction input amount", firstTransaction.getInputs().get(0).getAmount(), 
        firstUncompressedTr.getInputs().get(0).getAmount());
    assertEquals("verify correct block first transaction input previous output sequence number", 
        firstTransaction.getInputs().get(0).getPreviousOutputTransaction().getSequenceNumber(), 
        firstUncompressedTr.getInputs().get(0).getPreviousOutputTransaction().getSequenceNumber());
    assertEquals("verify correct block first transaction input previous output hash", 
        firstTransaction.getInputs().get(0).getPreviousOutputTransaction().getTransactionHash(), 
        firstUncompressedTr.getInputs().get(0).getPreviousOutputTransaction().getTransactionHash());
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
    assertEquals("verify correct block first transaction locktime", firstTransaction.getLockTime(), firstUncompressedTr.getLockTime());
    assertEquals("verify correct block ", fakeBlock.getVersion(), uncompressedBlock.get().getVersion());
  }
  
  @Test
  public void testCompressUncompress_withEmptyBlock_shouldDoCorrectly() {
    Block fakeBlock = new Block();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 2l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 3l, entity.getEntity().length);
    
    // decompress
    Optional<Block> uncompressedBlock = utils.decompress(entity.getEntity(), entity.getOriginalSize(), Block.class);
    
    assertTrue("verify that uncompressed Block is present", uncompressedBlock.isPresent());
    assertNotNull("verify that uncompressed block is not null", uncompressedBlock.get());
  }
  
  @Test
  public void testUncompress_withWrongBlockSize_shouldThrowException() {
    Block fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 1505l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 582l, entity.getEntity().length);
    
    // decompress
    utils.decompress(entity.getEntity(), entity.getOriginalSize(), Block.class);
  }
  
  @Test
  public void testUncompress_withWrongType_shouldThrowException() {
    Block fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 1505l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 582l, entity.getEntity().length);
    
    // decompress
    utils.decompress(entity.getEntity(), entity.getOriginalSize(), Transaction.class);
  }
  
  
  
}