package com.flockinger.groschn.blockchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.ConsensusType;
import com.flockinger.groschn.blockchain.model.Consent;
import com.flockinger.groschn.blockchain.model.Message;
import com.flockinger.groschn.blockchain.model.MessagePayload;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.util.compress.CompressedEntity;
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
  
    
  public static Transaction createRewardTransaction(boolean onlyReward) {
    Transaction transaction = new Transaction();
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
  
  public static List<Transaction> createBlockTransactions(boolean rewardRewardStatementsOnly, boolean rewardTxOnly) {
    List<Transaction> transactions = new ArrayList<>();
    
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
  
  public static Message<MessagePayload> validMessage() {
    Message<MessagePayload> message = new Message<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(1000l);
    MessagePayload txMessage = new MessagePayload();
    CompressedEntity entity = CompressedEntity.build().originalSize(123).entity(new byte[10]);
    txMessage.setEntity(entity);
    txMessage.setSenderId(UUID.randomUUID().toString());
    message.setPayload(txMessage);
    return message;
  }
}
