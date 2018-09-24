package com.flockinger.groschn.blockchain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.BlockInfo;
import com.flockinger.groschn.blockchain.model.Consent;
import com.flockinger.groschn.blockchain.model.SyncRequest;
import com.flockinger.groschn.blockchain.model.SyncResponse;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.util.hash.MultiHashGenerator;
import com.flockinger.groschn.blockchain.util.serialize.FstSerializer;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {MerkleRootCalculator.class, MultiHashGenerator.class})
@Import({CryptoConfig.class, TestConfig.class})
public class MerkleRootCalculatorTest {
  
  @Autowired
  private MerkleRootCalculator calc;
  
  @Test
  public void testCalculateMerkleRootHash_withFullListOfObjects_shouldReturnCorrect() {
    String rootHash = calc.calculateMerkleRootHash(fakeTransactions(9,false));
    
    assertNotNull("verify root hash is not null", rootHash);
    
    String rootHashOneTransactionLess = calc.calculateMerkleRootHash(fakeTransactions(8, false));
    assertNotEquals("verify with one transaction missing the root is different", 
        rootHash, rootHashOneTransactionLess);
    
    String rootHashSlightlyModified = calc.calculateMerkleRootHash(fakeTransactions(9, true));
    assertNotEquals("verify that with the slightest change the outcome is different", 
        rootHash, rootHashSlightlyModified);
  }
  
  @Test
  public void testCalculateMerkleRootHashAndTestSorting_withShuffledEntities_shouldReturnSameResult() {
    
    var transactions = fakeTransactions(9,false);
    String rootHash = calc.calculateMerkleRootHash(transactions);
    assertNotNull("verify root hash is not null", rootHash);
    Collections.shuffle(transactions);
    String shuffledRootHash = calc.calculateMerkleRootHash(transactions);
    assertEquals("verify that shuffled list results in same root-hash", rootHash, shuffledRootHash);
    
    var blocks = fakeBlocks();
    String rootHash2 = calc.calculateMerkleRootHash(blocks);
    Collections.shuffle(blocks);
    String shuffledRootHash2 = calc.calculateMerkleRootHash(blocks);
    assertEquals("verify that shuffled list results in same root-hash", rootHash2, shuffledRootHash2);
    
    var blockInfos = fakeBlockInfos();
    String rootHash3 = calc.calculateMerkleRootHash(blockInfos);
    Collections.shuffle(blockInfos);
    String shuffledRootHash3 = calc.calculateMerkleRootHash(blockInfos);
    assertEquals("verify that shuffled list results in same root-hash", rootHash3, shuffledRootHash3);
    
    var consents = fakeConsents();
    String rootHash4 = calc.calculateMerkleRootHash(consents);
    Collections.shuffle(consents);
    String shuffledRootHash4 = calc.calculateMerkleRootHash(consents);
    assertEquals("verify that shuffled list results in same root-hash", rootHash4, shuffledRootHash4);
    
    var responses = fakeSyncResponse();
    String rootHash5 = calc.calculateMerkleRootHash(responses);
    Collections.shuffle(responses);
    String shuffledRootHash5 = calc.calculateMerkleRootHash(responses);
    assertEquals("verify that shuffled list results in same root-hash", rootHash5, shuffledRootHash5);
    
    var requests = fakeSyncRequest();
    String rootHash6 = calc.calculateMerkleRootHash(requests);
    assertNotNull("verify root hash is not null", rootHash);
    Collections.shuffle(requests);
    String shuffledRootHash6 = calc.calculateMerkleRootHash(requests);
    assertEquals("verify that shuffled list results in same root-hash", rootHash6, shuffledRootHash6);
  }
  
  @Test
  public void testCalculateMerkleRootHash_withOneObject_shouldReturnCorrect() {
    String rootHash = calc.calculateMerkleRootHash(fakeTransactions(1,false));
    assertNotNull("verify root hash is not null", rootHash);
    assertFalse("verify that root hash is correct", rootHash.isEmpty());
  }
  
  @Test(expected = HashingException.class)
  public void testCalculateMerkleRootHash_withEmptyList_shouldStillReturnSomething() {
    calc.calculateMerkleRootHash(new ArrayList<Block>());
  }
  
  private List<Block> fakeBlocks() {
    var blocks = new ArrayList<Block>();
    for(long count=0;count<20;count++) {
      Block block = new Block();
      block.setPosition(count);
      block.setHash(UUID.randomUUID().toString());
      blocks.add(block);
    }
    return blocks;
  }
  private List<BlockInfo> fakeBlockInfos() {
    var blocks = new ArrayList<BlockInfo>();
    for(long count=0;count<20;count++) {
      BlockInfo blockInfo = new BlockInfo();
      blockInfo.setPosition(count);
      blockInfo.setBlockHash(UUID.randomUUID().toString());
      blocks.add(blockInfo);
    }
    return blocks;
  }
  private List<Consent> fakeConsents() {
    var blocks = new ArrayList<Consent>();
    for(long count=0;count<20;count++) {
      Consent consent = new Consent();
      consent.setTimestamp(count);
      blocks.add(consent);
    }
    return blocks;
  }
  private List<SyncResponse> fakeSyncResponse() {
    var responses = new ArrayList<SyncResponse>();
    for(long count=0;count<20;count++) {
      SyncResponse response = new SyncResponse();
      response.setStartingPosition(count);
      responses.add(response);
    }
    return responses;
  }
  private List<SyncRequest> fakeSyncRequest() {
    var requests = new ArrayList<SyncRequest>();
    for(long count=0;count<20;count++) {
      SyncRequest request = new SyncRequest();
      request.setStartingPosition(count);
      requests.add(request);
    }
    return requests;
  }
  
  public static List<Transaction> fakeTransactions(int size, boolean modifyLittleStuff) {
    Transaction tra1 = new Transaction();
    tra1.setInputs(ImmutableList.of(fakeInput(86l), fakeInput(14l)));
    tra1.setOutputs(ImmutableList.of(fakeInput(27l), fakeInput(73l)));
    tra1.setLockTime(934857l);
    tra1.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra2 = new Transaction();
    tra2.setInputs(ImmutableList.of(fakeInput(6l), fakeInput(4l)));
    tra2.setOutputs(ImmutableList.of(fakeInput(7l), fakeInput(3l)));
    tra2.setLockTime(87687l);
    tra2.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra3 = new Transaction();
    tra3.setInputs(ImmutableList.of(fakeInput(9996l), fakeInput(9994l)));
    tra3.setOutputs(ImmutableList.of(fakeInput(9997l), fakeInput(9993l)));
    tra3.setLockTime(432l);
    tra3.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra4 = new Transaction();
    tra4.setInputs(ImmutableList.of(fakeInput(670006l), fakeInput(670004l)));
    tra4.setOutputs(ImmutableList.of(fakeInput(670007l), fakeInput(670003l)));
    tra4.setLockTime(987l);
    tra4.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra5 = new Transaction();
    tra5.setInputs(ImmutableList.of(fakeInput(3406l), fakeInput(3404l)));
    tra5.setOutputs(ImmutableList.of(fakeInput(3407l), fakeInput(3403l)));
    tra5.setLockTime(46547l);
    tra5.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra6 = new Transaction();
    tra6.setInputs(ImmutableList.of(fakeInput(106l), fakeInput(104l)));
    tra6.setOutputs(ImmutableList.of(fakeInput(107l), fakeInput(103l)));
    tra6.setLockTime(798678l);
    tra6.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra7 = new Transaction();
    tra7.setInputs(ImmutableList.of(fakeInput(6006l), fakeInput(6004l)));
    tra7.setOutputs(ImmutableList.of(fakeInput(6007l), fakeInput(6003l)));
    tra7.setLockTime(5423454l);
    tra7.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra8 = new Transaction();
    tra8.setInputs(ImmutableList.of(fakeInput(5006l), fakeInput(5004l)));
    tra8.setOutputs(ImmutableList.of(fakeInput(5007l), fakeInput(5003l)));
    tra8.setLockTime(5423454l);
    tra8.setTransactionHash(UUID.randomUUID().toString());
    
    Transaction tra9 = new Transaction();
    tra9.setInputs(ImmutableList.of(fakeInput(4006l), fakeInput(4004l)));
    tra9.setOutputs(ImmutableList.of(fakeInput(4007l), fakeInput(4003l)));
    tra9.setLockTime(5423454l);
    tra9.setTransactionHash(UUID.randomUUID().toString());
    
    if(modifyLittleStuff) {
      tra9.getOutputs().get(0).setTimestamp(1234568l);
    }
    
    List<Transaction> transactions = new ArrayList<>();
    transactions.addAll(ImmutableList.of(tra1, tra2, tra3, tra4, tra5));
    transactions.addAll(ImmutableList.of(tra6, tra7, tra8, tra9));
    List<Transaction> minList = new ArrayList<>();
    minList.addAll(transactions.subList(0, size));
    return minList;
  }
  
  public static TransactionInput fakeInput(long amount) {
    TransactionInput input = new TransactionInput();
    input.setAmount(new BigDecimal(amount));
    input.setPublicKey("keykey");
    input.setTimestamp(1234567l);
    
    return input;
  }
}
