package com.flockinger.groschn.commons;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import com.flockinger.groschn.commons.compress.CompressedEntity;
import com.flockinger.groschn.commons.model.TestBlock;
import com.flockinger.groschn.commons.model.TestConsensusType;
import com.flockinger.groschn.commons.model.TestConsent;
import com.flockinger.groschn.commons.model.TestMessage;
import com.flockinger.groschn.commons.model.TestMessagePayload;
import com.flockinger.groschn.commons.model.TestTransaction;
import com.flockinger.groschn.commons.model.TestTransactionInput;
import com.flockinger.groschn.commons.model.TestTransactionOutput;
import com.google.common.collect.ImmutableList;

public class TestDataFactory {
  
  public static ModelMapper mapper = new ModelMapper();
  
  public static TestBlock getFakeBlock() {
    TestBlock block = new TestBlock();
    block.setPosition(97l);
    block.setTimestamp(new Date(20000).getTime());
    block.setHash("0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    TestConsent powConsent = new TestConsent();
    powConsent.setType(TestConsensusType.PROOF_OF_WORK);
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
  
  
  public static List<TestTransaction> fakeTransactions() {
    TestTransaction tra1 = new TestTransaction();
    tra1.setInputs(ImmutableList.of(fakeInput(86l), fakeInput(14l)));
    tra1.setOutputs(ImmutableList.of(fakeOutput(27l), fakeOutput(73l)));
    tra1.setLockTime(934857l);
    
    TestTransaction tra2 = new TestTransaction();
    tra2.setInputs(ImmutableList.of(fakeInput(6l), fakeInput(4l)));
    tra2.setOutputs(ImmutableList.of(fakeOutput(7l), fakeOutput(3l)));
    tra2.setLockTime(87687l);
       
    List<TestTransaction> transactions = new ArrayList<>();
    transactions.addAll(ImmutableList.of(tra1, tra2));
    return transactions;
  }
  
  public static TestTransactionInput fakeInput(String amount) {
    TestTransactionInput input = new TestTransactionInput();
    input.setAmount(new BigDecimal(amount));
    input.setPublicKey("keykey");
    input.setTimestamp(1234567l);
    input.setSignature("xxx");
    input.setSequenceNumber(3l);
    
    return input;
  }
  
  public static TestTransactionInput fakeInput(long amount) {
    return fakeInput(Long.toString(amount));
  }
  
  public static TestTransactionOutput fakeOutput(String amount) {
    TestTransactionOutput output = new TestTransactionOutput();
    output.setAmount(new BigDecimal(amount));
    output.setPublicKey("keykey");
    output.setTimestamp(1234567l);
    
    return output;
  }
  
  public static TestTransactionOutput fakeOutput(long amount) {    
    return fakeOutput(Long.toString(amount));
  }
  
    
  public static TestTransaction createRewardTransaction(boolean onlyReward) {
    TestTransaction transaction = new TestTransaction();
    transaction.setTransactionHash("0FABDD34578");
    List<TestTransactionInput> inputs = new ArrayList<>();
    TestTransactionInput input1 = new TestTransactionInput();
    input1.setAmount(new BigDecimal("100"));
    input1.setPublicKey("minerKey");
    input1.setSequenceNumber(1l);
    input1.setSignature("x0x0x0");
    input1.setTimestamp(new Date().getTime() - 4000l);
    inputs.add(input1);
    if(!onlyReward) {
      TestTransactionInput input2 = new TestTransactionInput();
      input2.setAmount(new BigDecimal("100"));
      input2.setPublicKey("very-secret2");
      input2.setSequenceNumber(2l);
      input2.setSignature("x1x1x1");
      input2.setTimestamp(new Date().getTime() - 100l);
      inputs.add(input2);
      TestTransactionInput input3 = new TestTransactionInput();
      input3.setAmount(new BigDecimal("300"));
      input3.setPublicKey("minerKey");
      input3.setSequenceNumber(3l);
      input3.setSignature("x2x2x2");
      input3.setTimestamp(new Date().getTime() - 10l);
      inputs.add(input3);
    }
    transaction.setInputs(inputs);
    List<TestTransactionOutput> outputs = new ArrayList<>();
    TestTransactionOutput out1 = new TestTransactionOutput(); // reward
    out1.setAmount(new BigDecimal("100"));
    out1.setPublicKey("minerKey");
    out1.setSequenceNumber(1l);
    out1.setTimestamp(new Date().getTime() - 5000l);
    outputs.add(out1);
    if(!onlyReward) {
      TestTransactionOutput out2 = new TestTransactionOutput();
      out2.setAmount(new BigDecimal("99"));
      out2.setPublicKey("very-secret2");
      out2.setSequenceNumber(2l);
      out2.setTimestamp(new Date().getTime() - 500l);
      outputs.add(out2);
      TestTransactionOutput out3 = new TestTransactionOutput();
      out3.setAmount(new BigDecimal("300"));
      out3.setPublicKey("minerKey");
      out3.setSequenceNumber(3l);
      out3.setTimestamp(new Date().getTime() - 50l);
      outputs.add(out3);
    }
    TestTransactionOutput out4 = new TestTransactionOutput(); //change
    out4.setAmount(new BigDecimal("12"));
    out4.setPublicKey("minerKey");
    out4.setSequenceNumber(onlyReward ? 2l : 4l);
    out4.setTimestamp(new Date().getTime() - 25l);
    outputs.add(out4);
    transaction.setOutputs(outputs);
    return transaction;
  }
  
  public static TestTransaction createValidTransaction(String expense1, String expense2, String expense3, String income1) {
    TestTransaction transaction = new TestTransaction();
    transaction.setTransactionHash("0FABDD34578");
    List<TestTransactionInput> inputs = new ArrayList<>();
    TestTransactionInput input1 = new TestTransactionInput();
    input1.setAmount(new BigDecimal("100"));
    input1.setPublicKey(expense1);
    input1.setSequenceNumber(1l);
    input1.setSignature("x0x0x0");
    input1.setTimestamp(new Date().getTime() - 4000l);
    inputs.add(input1);
    TestTransactionInput input2 = new TestTransactionInput();
    input2.setAmount(new BigDecimal("200"));
    input2.setPublicKey(expense2);
    input2.setSequenceNumber(2l);
    input2.setSignature("x1x1x1");
    input2.setTimestamp(new Date().getTime() - 100l);
    inputs.add(input2);
    TestTransactionInput input3 = new TestTransactionInput();
    input3.setAmount(new BigDecimal("300"));
    input3.setPublicKey(expense3);
    input3.setSequenceNumber(3l);
    input3.setSignature("x2x2x2");
    input3.setTimestamp(new Date().getTime() - 10l);
    inputs.add(input3);
    transaction.setInputs(inputs);
    List<TestTransactionOutput> outputs = new ArrayList<>();
    TestTransactionOutput out1 = new TestTransactionOutput();
    out1.setAmount(new BigDecimal("99"));
    out1.setPublicKey(expense2);
    out1.setSequenceNumber(1l);
    out1.setTimestamp(new Date().getTime() - 5000l);
    outputs.add(out1);
    TestTransactionOutput out2 = new TestTransactionOutput();
    out2.setAmount(new BigDecimal("350"));
    out2.setPublicKey(expense1);
    out2.setSequenceNumber(2l);
    out2.setTimestamp(new Date().getTime() - 500l);
    outputs.add(out2);
    TestTransactionOutput out3 = new TestTransactionOutput();
    out3.setAmount(new BigDecimal("150"));
    out3.setPublicKey(income1);
    out3.setSequenceNumber(3l);
    out3.setTimestamp(new Date().getTime() - 50l);
    outputs.add(out3);
    transaction.setOutputs(outputs);
    return transaction;
  }
  
  public static List<TestTransaction> createBlockTransactions(boolean rewardRewardStatementsOnly, boolean rewardTxOnly) {
    List<TestTransaction> transactions = new ArrayList<>();
    
    if(!rewardTxOnly) {
      transactions.add(createValidTransaction("someone1", "someone2", "someone3", "great1"));
      transactions.add(createValidTransaction("great1", "someone4", "someone5", "great1"));
      transactions.add(createValidTransaction("someone6", "someone7", "anotherone3", "someone4"));
      transactions.add(createValidTransaction("anotherone2", "anotherone4", "anotherone1", "someone7"));
    }
    transactions.add(createRewardTransaction(rewardRewardStatementsOnly));
    if(!rewardTxOnly) {
      transactions.add(createValidTransaction("some1", "some2", "some3", "great21"));
      transactions.add(createValidTransaction("great21", "some4", "some5", "great21"));
      transactions.add(createValidTransaction("some6", "some7", "another3", "some4"));
      transactions.add(createValidTransaction("another2", "another4", "another1", "some7"));
      transactions.add(createValidTransaction("great31", "someone24", "someone25", "great31"));
      transactions.add(createValidTransaction("some26", "someone27", "anotherone23", "someone24"));
      transactions.add(createValidTransaction("anotherone22", "anotherone24", "anotherone21", "someone25"));
    }
    return transactions;
  }
  
  public static TestMessage<TestMessagePayload> validMessage() {
    TestMessage<TestMessagePayload> message = new TestMessage<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(1000l);
    TestMessagePayload txMessage = new TestMessagePayload();
    CompressedEntity entity = CompressedEntity.build().originalSize(123).entity(new byte[10]);
    txMessage.setEntity(entity);
    txMessage.setSenderId(UUID.randomUUID().toString());
    message.setPayload(txMessage);
    return message;
  }
}
