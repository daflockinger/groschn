package com.flockinger.groschn.commons.hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.flockinger.groschn.commons.exception.HashingException;
import com.flockinger.groschn.commons.model.TestBlock;
import com.flockinger.groschn.commons.model.TestBlockInfo;
import com.flockinger.groschn.commons.model.TestConsent;
import com.flockinger.groschn.commons.model.TestSyncRequest;
import com.flockinger.groschn.commons.model.TestSyncResponse;
import com.flockinger.groschn.commons.model.TestTransaction;
import com.flockinger.groschn.commons.model.TestTransactionInput;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

public class MerkleRootCalculatorTest {

  private final MerkleRootCalculator calc = new MerkleRootCalculator();
  private final HashGenerator hasher = new MultiHashGenerator(calc);

  @BeforeClass
  public static void setup() {
    Provider bouncyCastle = new BouncyCastleProvider();
    Security.addProvider(bouncyCastle);
  }
  
  @Test
  public void testCalculateMerkleRootHash_withFullListOfObjects_shouldReturnCorrect() {
    String rootHash = calc.calculateMerkleRootHash(hasher, fakeTransactions(9,false));
    
    assertNotNull("verify root hash is not null", rootHash);
    
    String rootHashOneTransactionLess = calc.calculateMerkleRootHash(hasher,fakeTransactions(8, false));
    assertNotEquals("verify with one transaction missing the root is different", 
        rootHash, rootHashOneTransactionLess);
    
    String rootHashSlightlyModified = calc.calculateMerkleRootHash(hasher,fakeTransactions(9, true));
    assertNotEquals("verify that with the slightest change the outcome is different", 
        rootHash, rootHashSlightlyModified);
  }
  
  @Test
  public void testCalculateMerkleRootHashAndTestSorting_withShuffledEntities_shouldReturnSameResult() {
    var transactions = fakeTransactions(9,false);
    String rootHash = calc.calculateMerkleRootHash(hasher,transactions);
    assertNotNull("verify root hash is not null", rootHash);
    Collections.shuffle(transactions);
    String shuffledRootHash = calc.calculateMerkleRootHash(hasher,transactions);
    assertEquals("verify that shuffled list results in same root-hash", rootHash, shuffledRootHash);
    
    var blocks = fakeBlocks();
    String rootHash2 = calc.calculateMerkleRootHash(hasher,blocks);
    Collections.shuffle(blocks);
    String shuffledRootHash2 = calc.calculateMerkleRootHash(hasher,blocks);
    assertEquals("verify that shuffled list results in same root-hash", rootHash2, shuffledRootHash2);
    
    var blockInfos = fakeBlockInfos();
    String rootHash3 = calc.calculateMerkleRootHash(hasher,blockInfos);
    Collections.shuffle(blockInfos);
    String shuffledRootHash3 = calc.calculateMerkleRootHash(hasher,blockInfos);
    assertEquals("verify that shuffled list results in same root-hash", rootHash3, shuffledRootHash3);
    
    var consents = fakeConsents();
    String rootHash4 = calc.calculateMerkleRootHash(hasher,consents);
    Collections.shuffle(consents);
    String shuffledRootHash4 = calc.calculateMerkleRootHash(hasher,consents);
    assertEquals("verify that shuffled list results in same root-hash", rootHash4, shuffledRootHash4);
    
    var responses = fakeSyncResponse();
    String rootHash5 = calc.calculateMerkleRootHash(hasher,responses);
    Collections.shuffle(responses);
    String shuffledRootHash5 = calc.calculateMerkleRootHash(hasher,responses);
    assertEquals("verify that shuffled list results in same root-hash", rootHash5, shuffledRootHash5);
    
    var requests = fakeSyncRequest();
    String rootHash6 = calc.calculateMerkleRootHash(hasher,requests);
    assertNotNull("verify root hash is not null", rootHash6);
    Collections.shuffle(requests);
    String shuffledRootHash6 = calc.calculateMerkleRootHash(hasher,requests);
    assertEquals("verify that shuffled list results in same root-hash", rootHash6, shuffledRootHash6);
  }
  
  @Test
  public void testCalculateMerkleRootHash_withOneObject_shouldReturnCorrect() {
    String rootHash = calc.calculateMerkleRootHash(hasher,fakeTransactions(1,false));
    assertNotNull("verify root hash is not null", rootHash);
    assertFalse("verify that root hash is correct", rootHash.isEmpty());
  }
  
  @Test(expected = HashingException.class)
  public void testCalculateMerkleRootHash_withEmptyList_shouldStillReturnSomething() {
    calc.calculateMerkleRootHash(hasher,new ArrayList<TestBlock>());
  }
  
  private List<TestBlock> fakeBlocks() {
    var blocks = new ArrayList<TestBlock>();
    for(long count=0;count<20;count++) {
      TestBlock block = new TestBlock();
      block.setPosition(count);
      block.setHash(UUID.randomUUID().toString());
      blocks.add(block);
    }
    return blocks;
  }
  private List<TestBlockInfo> fakeBlockInfos() {
    var blocks = new ArrayList<TestBlockInfo>();
    for(long count=0;count<20;count++) {
      TestBlockInfo blockInfo = new TestBlockInfo();
      blockInfo.setPosition(count);
      blockInfo.setBlockHash(UUID.randomUUID().toString());
      blocks.add(blockInfo);
    }
    return blocks;
  }
  private List<TestConsent> fakeConsents() {
    var blocks = new ArrayList<TestConsent>();
    for(long count=0;count<20;count++) {
      TestConsent consent = new TestConsent();
      consent.setTimestamp(count);
      blocks.add(consent);
    }
    return blocks;
  }
  private List<TestSyncResponse> fakeSyncResponse() {
    var responses = new ArrayList<TestSyncResponse>();
    for(long count=0;count<20;count++) {
      TestSyncResponse response = new TestSyncResponse();
      response.setStartingPosition(count);
      responses.add(response);
    }
    return responses;
  }
  private List<TestSyncRequest> fakeSyncRequest() {
    var requests = new ArrayList<TestSyncRequest>();
    for(long count=0;count<20;count++) {
      TestSyncRequest request = new TestSyncRequest();
      request.setStartingPosition(count);
      requests.add(request);
    }
    return requests;
  }
  
  private static List<TestTransaction> fakeTransactions(int size, boolean modifyLittleStuff) {
    TestTransaction tra1 = new TestTransaction();
    tra1.setInputs(ImmutableList.of(fakeInput(86L), fakeInput(14L)));
    tra1.setOutputs(ImmutableList.of(fakeInput(27L), fakeInput(73L)));
    tra1.setLockTime(934857L);
    tra1.setTransactionHash("1");
    
    TestTransaction tra2 = new TestTransaction();
    tra2.setInputs(ImmutableList.of(fakeInput(6L), fakeInput(4L)));
    tra2.setOutputs(ImmutableList.of(fakeInput(7L), fakeInput(3L)));
    tra2.setLockTime(87687L);
    tra2.setTransactionHash("2");
    
    TestTransaction tra3 = new TestTransaction();
    tra3.setInputs(ImmutableList.of(fakeInput(9996L), fakeInput(9994L)));
    tra3.setOutputs(ImmutableList.of(fakeInput(9997L), fakeInput(9993L)));
    tra3.setLockTime(432L);
    tra3.setTransactionHash("3");
    
    TestTransaction tra4 = new TestTransaction();
    tra4.setInputs(ImmutableList.of(fakeInput(670006L), fakeInput(670004L)));
    tra4.setOutputs(ImmutableList.of(fakeInput(670007L), fakeInput(670003L)));
    tra4.setLockTime(987L);
    tra4.setTransactionHash("4");
    
    TestTransaction tra5 = new TestTransaction();
    tra5.setInputs(ImmutableList.of(fakeInput(3406L), fakeInput(3404L)));
    tra5.setOutputs(ImmutableList.of(fakeInput(3407L), fakeInput(3403L)));
    tra5.setLockTime(46547L);
    tra5.setTransactionHash("5");
    
    TestTransaction tra6 = new TestTransaction();
    tra6.setInputs(ImmutableList.of(fakeInput(106L), fakeInput(104L)));
    tra6.setOutputs(ImmutableList.of(fakeInput(107L), fakeInput(103L)));
    tra6.setLockTime(798678L);
    tra6.setTransactionHash("6");
    
    TestTransaction tra7 = new TestTransaction();
    tra7.setInputs(ImmutableList.of(fakeInput(6006L), fakeInput(6004L)));
    tra7.setOutputs(ImmutableList.of(fakeInput(6007L), fakeInput(6003L)));
    tra7.setLockTime(5423454L);
    tra7.setTransactionHash("7");
    
    TestTransaction tra8 = new TestTransaction();
    tra8.setInputs(ImmutableList.of(fakeInput(5006L), fakeInput(5004L)));
    tra8.setOutputs(ImmutableList.of(fakeInput(5007L), fakeInput(5003L)));
    tra8.setLockTime(5423454L);
    tra8.setTransactionHash("8");
    
    TestTransaction tra9 = new TestTransaction();
    tra9.setInputs(ImmutableList.of(fakeInput(4006L), fakeInput(4004L)));
    tra9.setOutputs(ImmutableList.of(fakeInput(4007L), fakeInput(4003L)));
    tra9.setLockTime(5423454L);
    tra9.setTransactionHash("9");
    
    if(modifyLittleStuff) {
      tra9.getOutputs().get(0).setTimestamp(1234568L);
    }
    
    List<TestTransaction> transactions = new ArrayList<>();
    transactions.addAll(ImmutableList.of(tra1, tra2, tra3, tra4, tra5));
    transactions.addAll(ImmutableList.of(tra6, tra7, tra8, tra9));
    return new ArrayList<>(transactions.subList(0, size));
  }
  
  private static TestTransactionInput fakeInput(long amount) {
    TestTransactionInput input = new TestTransactionInput();
    input.setAmount(new BigDecimal(amount));
    input.setPublicKey("keykey");
    input.setTimestamp(1234567L);
    
    return input;
  }
}
