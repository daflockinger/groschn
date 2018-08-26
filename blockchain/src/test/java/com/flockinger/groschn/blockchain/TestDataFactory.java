package com.flockinger.groschn.blockchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.RandomUtils;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.model.TransactionPointCut;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionInput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionOutput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionPointCut;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.google.common.collect.ImmutableList;

public class TestDataFactory {
  
  
  public static Block getFakeBlock() {
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
  
  public static TransactionInput fakeInput(String amount) {
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
  
  public static TransactionInput fakeInput(long amount) {
    return fakeInput(Long.toString(amount));
  }
  
  public static TransactionOutput fakeOutput(String amount) {
    TransactionOutput output = new TransactionOutput();
    output.setAmount(new BigDecimal(amount));
    output.setPublicKey("keykey");
    output.setTimestamp(1234567l);
    
    return output;
  }
  
  public static TransactionOutput fakeOutput(long amount) {    
    return fakeOutput(Long.toString(amount));
  }
  
  
  public static Assessment fakeAssessment(boolean success) {
    Assessment am = new Assessment();
    if(!success) {
      am.setReasonOfFailure("Bad luck this time");
    }
    am.setValid(success);
    return am;
  }
  
  
  public static StoredTransactionOutput createRandomTransactionOutputWith(long sequenceNumber, String pubKey, Long amount) {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal(amount));
    output.setPublicKey(pubKey);
    output.setSequenceNumber(sequenceNumber);
    output.setTimestamp(new Date().getTime());
    return output;
  }
  public static StoredTransactionOutput createRandomTransactionOutputWith(long sequenceNumber, String pubKey) {
    return createRandomTransactionOutputWith(sequenceNumber, pubKey, RandomUtils.nextLong(1, 101));
  }
  public static StoredTransactionOutput createRandomTransactionOutputWith(long sequenceNumber) {
    return createRandomTransactionOutputWith(sequenceNumber, UUID.randomUUID().toString());
  }
  
  
  public static StoredTransactionInput createRandomTransactionInputWith(long sequenceNumber, String pubKey, Long amount) {
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
  public static StoredTransactionInput createRandomTransactionInputWith(long sequenceNumber, String pubKey) {
    return createRandomTransactionInputWith(sequenceNumber, pubKey, RandomUtils.nextLong(1, 101));
  }
  public static StoredTransactionInput createRandomTransactionInputWith(long sequenceNumber) {
    return createRandomTransactionInputWith(sequenceNumber, UUID.randomUUID().toString());
  }
  
  
  public static StoredTransaction createRandomTransactionWith(String importantHash, 
      StoredTransactionOutput importantOutput, StoredTransactionInput importantInput) {
    StoredTransaction transaction = new StoredTransaction();

    if(importantHash != null) {
      transaction.setTransactionHash(importantHash);
    } else {
      transaction.setTransactionHash(UUID.randomUUID().toString());
    }
    if(importantOutput != null) {
      transaction.setOutputs(ImmutableList.of(createRandomTransactionOutputWith(1), importantOutput, 
          createRandomTransactionOutputWith(3),createRandomTransactionOutputWith(4)));
    } else {
      transaction.setOutputs(ImmutableList.of(createRandomTransactionOutputWith(1),createRandomTransactionOutputWith(2),
          createRandomTransactionOutputWith(3),createRandomTransactionOutputWith(4)));
    }
    if(importantInput != null) {
      transaction.setInputs(ImmutableList.of(createRandomTransactionInputWith(1), importantInput, 
          createRandomTransactionInputWith(3),createRandomTransactionInputWith(4)));
    } else {
      transaction.setInputs(ImmutableList.of(createRandomTransactionInputWith(1),createRandomTransactionInputWith(2),
          createRandomTransactionInputWith(3),createRandomTransactionInputWith(4)));
    }
    return transaction;
  }
  
  public static StoredTransaction createRandomTransactionWiths(List<StoredTransactionInput> inputs, 
      List<StoredTransactionOutput> outputs) {
    StoredTransaction transaction = new StoredTransaction();
    if(inputs != null) {
      transaction.setInputs(inputs);
    } else {
      transaction.setInputs(ImmutableList.of(createRandomTransactionInputWith(1),createRandomTransactionInputWith(2),
          createRandomTransactionInputWith(3),createRandomTransactionInputWith(4)));
    }
    if(outputs != null) {
      transaction.setOutputs(outputs);
    } else {
      transaction.setOutputs(ImmutableList.of(createRandomTransactionOutputWith(1),createRandomTransactionOutputWith(2),
          createRandomTransactionOutputWith(3),createRandomTransactionOutputWith(4)));
    }
    return transaction;
  }
}
