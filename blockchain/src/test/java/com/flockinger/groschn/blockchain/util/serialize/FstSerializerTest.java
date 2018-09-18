package com.flockinger.groschn.blockchain.util.serialize;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.SerializationException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.repository.model.BlockProcess;
import com.flockinger.groschn.blockchain.util.serialize.impl.FstSerializer;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {FstSerializer.class})
public class FstSerializerTest {
  
  @Autowired
  private FstSerializer fstSerializer;
  
  @Test
  public void testSerializeDeserialize_withHugeBlock_shouldWorkWell() {
    Block fakeBlock =  TestDataFactory.getFakeBlock();
   
    byte[] serializedBlock = fstSerializer.serialize(fakeBlock);
    assertNotNull("verify serialized block is not null", serializedBlock);
    assertTrue("verify serialized block has some size to it", serializedBlock.length > 0);
    
    Block deserializedBlock = fstSerializer.deserialize(serializedBlock, Block.class);
    assertNotNull("verify that deserialized block is not null", deserializedBlock);
    assertEquals("verify correct block consent difficulty", fakeBlock.getConsent().getDifficulty(), deserializedBlock.getConsent().getDifficulty());
    assertEquals("verify correct block consent millisecondsspent", fakeBlock.getConsent().getMilliSecondsSpentMining(), deserializedBlock.getConsent().getMilliSecondsSpentMining());
    assertEquals("verify correct block consent nonce", fakeBlock.getConsent().getNonce(), deserializedBlock.getConsent().getNonce());
    assertEquals("verify correct block consent timestamp", fakeBlock.getConsent().getTimestamp(), deserializedBlock.getConsent().getTimestamp());
    assertEquals("verify correct block consent type", fakeBlock.getConsent().getType(), deserializedBlock.getConsent().getType());
    assertEquals("verify correct block hash ", fakeBlock.getHash(), deserializedBlock.getHash());
    assertEquals("verify correct block last hash", fakeBlock.getLastHash(), deserializedBlock.getLastHash());
    assertEquals("verify correct block position", fakeBlock.getPosition(), deserializedBlock.getPosition());
    assertEquals("verify correct block timestamp", fakeBlock.getTimestamp(), deserializedBlock.getTimestamp());
    assertEquals("verify correct block transactions merkle root", fakeBlock.getTransactionMerkleRoot(), deserializedBlock.getTransactionMerkleRoot());
    assertEquals("verify correct block transaction size", fakeBlock.getTransactions().size(), deserializedBlock.getTransactions().size());
    Transaction firstTransaction = fakeBlock.getTransactions().get(0);
    Transaction firstUncompressedTr = deserializedBlock.getTransactions().get(0);
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
    assertEquals("verify correct block ", fakeBlock.getVersion(), deserializedBlock.getVersion());
  }
  
  @Test
  public void testSerializeDeserialize_withSerializeEmptyList_shouldWorkWell() {
    List<String> empty =  new ArrayList<>();
   
    byte[] serializedBlock = fstSerializer.serialize(empty);
    assertNotNull("verify serialized block is not null", serializedBlock);
    assertTrue("verify serialized block has some size to it", serializedBlock.length > 0);
    
    List<String> emptyResult = fstSerializer.deserialize(serializedBlock, List.class);
    assertNotNull("verify that deserialized list is not null", emptyResult);
    assertTrue("verify that returned list is empty", emptyResult.isEmpty());
  }
  
  @Test
  public void testSerializeDeserialize_withConsentTypeEnum_shouldWorkWell() {
    ConsensusType type =  ConsensusType.PROOF_OF_MAJORITY;
   
    byte[] serializedBlock = fstSerializer.serialize(type);
    assertNotNull("verify serialized block is not null", serializedBlock);
    assertTrue("verify serialized block has some size to it", serializedBlock.length > 0);
    
    ConsensusType typeResult = fstSerializer.deserialize(serializedBlock, ConsensusType.class);
    assertNotNull("verify that deserialized list is not null", typeResult);
    assertEquals("verify that returned enum is correct", ConsensusType.PROOF_OF_MAJORITY, typeResult);
  }
  
  @Test
  public void testSerializeDeserialize_withListOfTransactions_shouldWorkWell() {
    List<Transaction> transactions = TestDataFactory.createBlockTransactions(false, false);
   
    byte[] serializedBlock = fstSerializer.serialize(transactions);
    assertNotNull("verify serialized block is not null", serializedBlock);
    assertTrue("verify serialized block has some size to it", serializedBlock.length > 0);
    
    List<Transaction> transactionResults = fstSerializer.deserialize(serializedBlock, List.class);
    assertNotNull("verify that deserialized list is not null", transactionResults);
    assertEquals("verify that returned transactions have correct size", 12, transactions.size());
    assertEquals("verify reward transacton miner pub key", "minerKey", transactions.get(4).getInputs().get(0).getPublicKey());
  }
  
  @Test(expected=SerializationException.class)
  public void testSerializeDeserialize_withNull_shouldThrowException() {
    byte[] serializedBlock = fstSerializer.serialize(null);
    assertNotNull("verify serialized block is not null", serializedBlock);
    assertTrue("verify serialized block has some size to it", serializedBlock.length > 0);
    
    Object transactionResults = fstSerializer.deserialize(serializedBlock, Object.class);
    assertNull("verify that result is null", transactionResults);
  }
  
  @Test(expected=SerializationException.class)
  public void testDeserialize_withEmptyBytes_shouldThrowException() {
    Object transactionResults = fstSerializer.deserialize(new byte[0], Object.class);
    assertNull("verify that result is null", transactionResults);
  }
  
  @Test(expected=SerializationException.class)
  public void testDeserialize_withWrongType_shouldWorkWell() {
    ConsensusType type =  ConsensusType.PROOF_OF_MAJORITY;
    byte[] serializedBlock = fstSerializer.serialize(type);
    
    fstSerializer.deserialize(serializedBlock, String.class);
  }
  
  @Test(expected=SerializationException.class)
  public void testSerialize_withNonSerializableType_shouldThrowException() {
    BlockProcess process =  new BlockProcess();
    fstSerializer.serialize(process);
  }
  
  @Test
  public void testSerializeDeserialize_withNotRegisteredType_shouldWorkWell() {
    MessagePayload message = new MessagePayload();
   
    byte[] serializedBlock = fstSerializer.serialize(message);
    assertNotNull("verify serialized block is not null", serializedBlock);
    assertTrue("verify serialized block has some size to it", serializedBlock.length > 0);
    
    MessagePayload messageRsult = fstSerializer.deserialize(serializedBlock, MessagePayload.class);
    assertNotNull("verify that deserialized list is not null", messageRsult);
  }
}
