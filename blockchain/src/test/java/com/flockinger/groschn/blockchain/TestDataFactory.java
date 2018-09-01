package com.flockinger.groschn.blockchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.RandomUtils;
import org.modelmapper.ModelMapper;
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
  
  public static ModelMapper mapper = new ModelMapper();
  
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
  
  
  public static Transaction mapToTransaction(StoredTransaction storedTransaction) {
    return mapper.map(storedTransaction, Transaction.class);
  }
  
  
  public static StoredTransactionOutput createRandomTransactionOutputWith(long sequenceNumber, String pubKey, Long amount, Long timestamp) {
    StoredTransactionOutput output = new StoredTransactionOutput();
    output.setAmount(new BigDecimal(amount));
    output.setPublicKey(pubKey);
    output.setSequenceNumber(sequenceNumber);
    output.setTimestamp(timestamp);
    return output;
  }
  public static StoredTransactionOutput createRandomTransactionOutputWith(long sequenceNumber, String pubKey, Long amount) {
    return createRandomTransactionOutputWith(sequenceNumber, pubKey, amount, new Date().getTime());
  }
  public static StoredTransactionOutput createRandomTransactionOutputWith(long sequenceNumber, String pubKey) {
    return createRandomTransactionOutputWith(sequenceNumber, pubKey, RandomUtils.nextLong(1, 101));
  }
  public static StoredTransactionOutput createRandomTransactionOutputWith(long sequenceNumber) {
    return createRandomTransactionOutputWith(sequenceNumber, UUID.randomUUID().toString());
  }
  
  
  public static StoredTransactionInput createRandomTransactionInputWith(long sequenceNumber, String pubKey, Long amount, Long timestamp) {
    StoredTransactionInput input = new StoredTransactionInput();
    input.setAmount(new BigDecimal(amount));
    input.setPublicKey(pubKey);
    input.setSequenceNumber(sequenceNumber);
    input.setTimestamp(timestamp);
    StoredTransactionPointCut pointcut = new StoredTransactionPointCut();
    pointcut.setSequenceNumber(RandomUtils.nextLong(1, 10));
    pointcut.setTransactionHash(UUID.randomUUID().toString());
    input.setPreviousOutputTransaction(pointcut);
    input.setSignature(UUID.randomUUID().toString());
    return input;
  }
  public static StoredTransactionInput createRandomTransactionInputWith(long sequenceNumber, String pubKey, Long amount) {
    return createRandomTransactionInputWith(sequenceNumber, pubKey, amount, new Date().getTime());   
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
  
  public static Transaction createRewardTransaction(boolean onlyReward) {
    Transaction transaction = new Transaction();
    transaction.setId(UUID.randomUUID().toString());
    transaction.setTransactionHash("0FABDD34578");
    List<TransactionInput> inputs = new ArrayList<>();
    TransactionInput input1 = new TransactionInput();
    input1.setAmount(new BigDecimal("100"));
    input1.setPublicKey("minerKey");
    input1.setSequenceNumber(1l);
    input1.setSignature("x0x0x0");
    input1.setTimestamp(new Date().getTime() - 4000l);
    inputs.add(input1);
    if(!onlyReward) {
      TransactionInput input2 = new TransactionInput();
      input2.setAmount(new BigDecimal("100"));
      input2.setPublicKey("very-secret2");
      input2.setSequenceNumber(2l);
      input2.setSignature("x1x1x1");
      input2.setTimestamp(new Date().getTime() - 100l);
      inputs.add(input2);
      TransactionInput input3 = new TransactionInput();
      input3.setAmount(new BigDecimal("300"));
      input3.setPublicKey("minerKey");
      input3.setSequenceNumber(3l);
      input3.setSignature("x2x2x2");
      input3.setTimestamp(new Date().getTime() - 10l);
      inputs.add(input3);
    }
    transaction.setInputs(inputs);
    List<TransactionOutput> outputs = new ArrayList<>();
    TransactionOutput out1 = new TransactionOutput(); // reward
    out1.setAmount(new BigDecimal("100"));
    out1.setPublicKey("minerKey");
    out1.setSequenceNumber(1l);
    out1.setTimestamp(new Date().getTime() - 5000l);
    outputs.add(out1);
    if(!onlyReward) {
      TransactionOutput out2 = new TransactionOutput();
      out2.setAmount(new BigDecimal("99"));
      out2.setPublicKey("very-secret2");
      out2.setSequenceNumber(2l);
      out2.setTimestamp(new Date().getTime() - 500l);
      outputs.add(out2);
      TransactionOutput out3 = new TransactionOutput();
      out3.setAmount(new BigDecimal("300"));
      out3.setPublicKey("minerKey");
      out3.setSequenceNumber(3l);
      out3.setTimestamp(new Date().getTime() - 50l);
      outputs.add(out3);
    }
    TransactionOutput out4 = new TransactionOutput(); //change
    out4.setAmount(new BigDecimal("12"));
    out4.setPublicKey("minerKey");
    out4.setSequenceNumber(onlyReward ? 2l : 4l);
    out4.setTimestamp(new Date().getTime() - 25l);
    outputs.add(out4);
    transaction.setOutputs(outputs);
    return transaction;
  }
  
  public static Transaction createValidTransaction(String expense1, String expense2, String expense3, String income1) {
    Transaction transaction = new Transaction();
    transaction.setId(UUID.randomUUID().toString());
    transaction.setTransactionHash("0FABDD34578");
    List<TransactionInput> inputs = new ArrayList<>();
    TransactionInput input1 = new TransactionInput();
    input1.setAmount(new BigDecimal("100"));
    input1.setPublicKey(expense1);
    input1.setSequenceNumber(1l);
    input1.setSignature("x0x0x0");
    input1.setTimestamp(new Date().getTime() - 4000l);
    inputs.add(input1);
    TransactionInput input2 = new TransactionInput();
    input2.setAmount(new BigDecimal("200"));
    input2.setPublicKey(expense2);
    input2.setSequenceNumber(2l);
    input2.setSignature("x1x1x1");
    input2.setTimestamp(new Date().getTime() - 100l);
    inputs.add(input2);
    TransactionInput input3 = new TransactionInput();
    input3.setAmount(new BigDecimal("300"));
    input3.setPublicKey(expense3);
    input3.setSequenceNumber(3l);
    input3.setSignature("x2x2x2");
    input3.setTimestamp(new Date().getTime() - 10l);
    inputs.add(input3);
    transaction.setInputs(inputs);
    List<TransactionOutput> outputs = new ArrayList<>();
    TransactionOutput out1 = new TransactionOutput();
    out1.setAmount(new BigDecimal("99"));
    out1.setPublicKey(expense2);
    out1.setSequenceNumber(1l);
    out1.setTimestamp(new Date().getTime() - 5000l);
    outputs.add(out1);
    TransactionOutput out2 = new TransactionOutput();
    out2.setAmount(new BigDecimal("350"));
    out2.setPublicKey(expense1);
    out2.setSequenceNumber(2l);
    out2.setTimestamp(new Date().getTime() - 500l);
    outputs.add(out2);
    TransactionOutput out3 = new TransactionOutput();
    out3.setAmount(new BigDecimal("150"));
    out3.setPublicKey(income1);
    out3.setSequenceNumber(3l);
    out3.setTimestamp(new Date().getTime() - 50l);
    outputs.add(out3);
    transaction.setOutputs(outputs);
    return transaction;
  }
}
