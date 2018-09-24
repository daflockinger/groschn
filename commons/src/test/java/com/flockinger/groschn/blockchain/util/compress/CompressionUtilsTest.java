package com.flockinger.groschn.blockchain.util.compress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.util.compress.CompressedEntity;
import com.flockinger.groschn.blockchain.util.compress.CompressionUtils;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CompressionUtils.class})
@Import(TestConfig.class)
public class CompressionUtilsTest {

  @Autowired
  private CompressionUtils utils;
  
  @Test
  public void testCompressUncompress_withFullBlock_shouldDoCorrectly() {
    Block fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 387l, entity.getEntity().length);
    
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
    assertEquals("verify correct block first transaction locktime", firstTransaction.getLockTime(), firstUncompressedTr.getLockTime());
    assertEquals("verify correct block ", fakeBlock.getVersion(), uncompressedBlock.get().getVersion());
  }
  
  @Test
  public void testCompressedByteSize_withSomeTransactions_shouldReturnCorrect() {
    int byteSize = utils.compressedByteSize(TestDataFactory.fakeTransactions());
    assertEquals("verify correct compressed size of entities", 197l, byteSize);
  }
  
  @Test
  public void testCompressedByteSize_withEmptyTransactions_shouldReturnCorrect() {
    int byteSize = utils.compressedByteSize(new ArrayList<Transaction>());
    assertEquals("verify correct compressed size of entities", 0, byteSize);
  }
  
  @Test
  public void testCompressUncompress_withEmptyBlock_shouldDoCorrectly() {
    Block fakeBlock = new Block();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 11l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 12l, entity.getEntity().length);
    
    // decompress
    Optional<Block> uncompressedBlock = utils.decompress(entity.getEntity(), entity.getOriginalSize(), Block.class);
    
    assertTrue("verify that uncompressed Block is present", uncompressedBlock.isPresent());
    assertNotNull("verify that uncompressed block is not null", uncompressedBlock.get());
  }
  
  @Test
  public void testUncompress_withWrongBlockSize_shouldReturnEmpty() {
    Block fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 387l, entity.getEntity().length);
    
    // decompress
    var decompressedResult = utils.decompress(entity.getEntity(), entity.getOriginalSize() + 2, Block.class);
    assertFalse("verify that returned entity of wrong type is empty", decompressedResult.isPresent());
  }
  
  @Test
  public void testUncompress_withWrongCompressedEntity_shouldReturnEmpty() {
    Block fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 387l, entity.getEntity().length);
    
    // decompress
    var decompressedResult = utils.decompress(new byte[10], entity.getOriginalSize(), Block.class);
    assertFalse("verify that returned entity of wrong type is empty", decompressedResult.isPresent());
  }
  
  @Test
  public void testUncompress_withWrongType_shouldReturnEmpty() {
    Block fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 387l, entity.getEntity().length);
    
    // decompress
    var decompressedResult = utils.decompress(entity.getEntity(), entity.getOriginalSize(), Transaction.class);
    
    assertFalse("verify that returned entity of wrong type is empty", decompressedResult.isPresent());
  }
}
