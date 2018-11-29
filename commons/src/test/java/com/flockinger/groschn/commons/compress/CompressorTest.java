package com.flockinger.groschn.commons.compress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.flockinger.groschn.commons.TestDataFactory;
import com.flockinger.groschn.commons.model.TestBlock;
import com.flockinger.groschn.commons.model.TestConsensusType;
import com.flockinger.groschn.commons.model.TestConsent;
import com.flockinger.groschn.commons.model.TestTransaction;
import com.flockinger.groschn.commons.model.TestTransactionInput;
import com.flockinger.groschn.commons.model.TestTransactionOutput;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Test;

public class CompressorTest {

  private BlockSerializer serializer = serializer();
  private Compressor compressor = new Compressor(serializer);
  
  @Test
  public void testCompressUncompress_withFullBlock_shouldDoCorrectly() {
    TestBlock fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = compressor.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 388, entity.getEntity().length);
    
    // decompress
    Optional<TestBlock> uncompressedBlock = compressor
        .decompress(entity.getEntity(), entity.getOriginalSize(), TestBlock.class);
    
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
    TestTransaction firstTransaction = fakeBlock.getTransactions().get(0);
    TestTransaction firstUncompressedTr = uncompressedBlock.get().getTransactions().get(0);
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
    int byteSize = compressor.compressedByteSize(TestDataFactory.fakeTransactions());
    assertEquals("verify correct compressed size of entities", 197l, byteSize);
  }
  
  @Test
  public void testCompressedByteSize_withEmptyTransactions_shouldReturnCorrect() {
    int byteSize = compressor.compressedByteSize(new ArrayList<TestTransaction>());
    assertEquals("verify correct compressed size of entities", 0, byteSize);
  }
  
  @Test
  public void testCompressUncompress_withEmptyBlock_shouldDoCorrectly() {
    TestBlock fakeBlock = new TestBlock();
    
    // compress
    CompressedEntity entity = compressor.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 11l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 12l, entity.getEntity().length);
    
    // decompress
    Optional<TestBlock> uncompressedBlock = compressor
        .decompress(entity.getEntity(), entity.getOriginalSize(), TestBlock.class);
    
    assertTrue("verify that uncompressed Block is present", uncompressedBlock.isPresent());
    assertNotNull("verify that uncompressed block is not null", uncompressedBlock.get());
  }
  
  @Test
  public void testUncompress_withWrongBlockSize_shouldReturnEmpty() {
    TestBlock fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = compressor.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 388, entity.getEntity().length);
    
    // decompress
    var decompressedResult = compressor
        .decompress(entity.getEntity(), entity.getOriginalSize() + 2, TestBlock.class);
    assertFalse("verify that returned entity of wrong type is empty", decompressedResult.isPresent());
  }
  
  @Test
  public void testUncompress_withWrongCompressedEntity_shouldReturnEmpty() {
    TestBlock fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = compressor.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 388, entity.getEntity().length);
    
    // decompress
    var decompressedResult = compressor.decompress(new byte[10], entity.getOriginalSize(), TestBlock.class);
    assertFalse("verify that returned entity of wrong type is empty", decompressedResult.isPresent());
  }
  
  @Test
  public void testUncompress_withWrongType_shouldReturnEmpty() {
    TestBlock fakeBlock = TestDataFactory.getFakeBlock();
    
    // compress
    CompressedEntity entity = compressor.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 629l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 388, entity.getEntity().length);
    
    // decompress
    var decompressedResult = compressor
        .decompress(entity.getEntity(), entity.getOriginalSize(), TestTransaction.class);
    
    assertFalse("verify that returned entity of wrong type is empty", decompressedResult.isPresent());
  }

  @Test
  public void testSerializer() {
    assertEquals("verify correct serializer is returned", serializer, compressor.serializer());
  }

  public BlockSerializer serializer() {
    var registered = new ArrayList<Class<?>>();
    registered.add(TestBlock.class);
    registered.add(TestTransaction.class);
    registered.add(TestTransactionInput.class);
    registered.add(TestTransactionOutput.class);
    registered.add(TestConsent.class);
    registered.add(TestConsensusType.class);

    return new FstSerializer(registered);
  }
}
