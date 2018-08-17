package com.flockinger.groschn.blockchain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.model.TransactionPointCut;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {CompressionUtils.class})
public class CompressionUtilsTest {

  @Autowired
  private CompressionUtils utils;
  
  @Test
  public void testCompressUncompress_withFullBlock_shouldDoCorrectly() {
    Block fakeBlock = getFakeBlock();
    
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
    Block fakeBlock = getFakeBlock();
    
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
    Block fakeBlock = getFakeBlock();
    
    // compress
    CompressedEntity entity = utils.compress(fakeBlock);
    
    assertNotNull("verify compressedEntity is returned not null", entity);
    assertEquals("verify correct original size", 1505l, entity.getOriginalSize());
    assertEquals("verify correct compressed size", 582l, entity.getEntity().length);
    
    // decompress
    utils.decompress(entity.getEntity(), entity.getOriginalSize(), Transaction.class);
  }
  
  private Block getFakeBlock() {
    Block block = new Block();
    block.setPosition(97l);
    block.setTimestamp(new Date(20000).getTime());
    block.setHash("0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    Consent powConsent = new Consent();
    powConsent.setType(ConsensusType.PROOF_OF_WORK);
    powConsent.setDifficulty(5);
    powConsent.setMilliSecondsSpentMining(12000l);
    powConsent.setNonce(123l);
    powConsent.setTimestamp(2342343545l);
    block.setConsent(powConsent);
    block.setLastHash("000cf8761b99932db819909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    block.setTransactionMerkleRoot("9678087");
    block.setTransactions(fakeTransactions());
    block.setVersion(1);
    return block;
  }
  
  
  public static List<Transaction> fakeTransactions() {
    Transaction tra1 = new Transaction();
    tra1.setId("2345");
    tra1.setInputs(ImmutableList.of(fakeInput(86l), fakeInput(14l)));
    tra1.setOutputs(ImmutableList.of(fakeOutput(27l), fakeOutput(73l)));
    tra1.setLockTime(934857l);
    
    Transaction tra2 = new Transaction();
    tra2.setInputs(ImmutableList.of(fakeInput(6l), fakeInput(4l)));
    tra2.setOutputs(ImmutableList.of(fakeOutput(7l), fakeOutput(3l)));
    tra2.setLockTime(87687l);
       
    List<Transaction> transactions = new ArrayList<>();
    transactions.addAll(ImmutableList.of(tra1, tra2));
    return transactions;
  }
  
  public static TransactionInput fakeInput(long amount) {
    TransactionInput input = new TransactionInput();
    input.setAmount(new BigDecimal(amount));
    input.setPublicKey("keykey");
    input.setTimestamp(1234567l);
    input.setSignature("xxx");
    input.setSequenceNumber(3l);
    TransactionPointCut pointcut = new TransactionPointCut();
    pointcut.setSequenceNumber(23423435l);
    pointcut.setTransactionHash("467547");
    input.setPreviousOutputTransaction(pointcut);
    
    return input;
  }
  
  public static TransactionOutput fakeOutput(long amount) {
    TransactionOutput output = new TransactionOutput();
    output.setAmount(new BigDecimal(amount));
    output.setPublicKey("keykey");
    output.setTimestamp(1234567l);
    
    return output;
  }
  
}
