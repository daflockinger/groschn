package com.flockinger.groschn.blockchain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomUtils;
import org.modelmapper.ModelMapper;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionInput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionOutput;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.commons.compress.CompressedEntity;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.model.SyncResponse;
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
    tra1.setInputs(statementList(fakeInput(86l), fakeInput(14l)));
    tra1.setOutputs(statementList(fakeOutput(27l), fakeOutput(73l)));
    tra1.setLockTime(934857l);
    
    Transaction tra2 = new Transaction();
    tra2.setInputs(statementList(fakeInput(6l), fakeInput(4l)));
    tra2.setOutputs(statementList(fakeOutput(7l), fakeOutput(3l)));
    tra2.setLockTime(87687l);
       
    List<Transaction> transactions = new ArrayList<>();
    transactions.addAll(ImmutableList.of(tra1, tra2));
    return transactions;
  }
  
  private static <T extends TransactionOutput> List<T> statementList(T... statements){
    return Arrays.asList(statements);
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
  
  
  public static Assessment fakeAssessment(boolean success) {
    Assessment am = new Assessment();
    if(!success) {
      am.setReasonOfFailure("Bad luck this time");
    }
    am.setValid(success);
    return am;
  }
  
  public static TransactionOutput mapToTransactionOutput(StoredTransactionOutput storedOutput) {
    return mapper.map(storedOutput, TransactionOutput.class);
  }
  
  public static TransactionInput mapToTransactionInput(StoredTransactionInput storedInput) {
    return mapper.map(storedInput, TransactionInput.class);
  }
  
  public static Transaction mapToTransaction(StoredTransaction storedTransaction) {
    return mapper.map(storedTransaction, Transaction.class);
  }
  
  public static StoredTransaction mapToStoredTransaction(Transaction transaction) {
    return mapper.map(transaction, StoredTransaction.class);
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
  
  public static List<Transaction> createRandomTransactions(Optional<String> minerKey,
      Optional<Long> amount, boolean noInput) {
    var transactions = new ArrayList<Transaction>();
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    if (minerKey.isPresent() && amount.isPresent()) {
      transactions.add(mapToTransaction(createRandomTransactionWith(null,
          createRandomTransactionOutputWith(2, minerKey.get(), amount.get()),
          (noInput) ? createRandomTransactionInputWith(2)
              : createRandomTransactionInputWith(2, minerKey.get(), amount.get()))));
    }
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    return transactions;
  }
  
  public static Transaction createRewardTransactionWithBalance(Long balance) {
    var transaction = createRewardTransaction(true);
    transaction.getInputs().add(mapToTransactionInput(createRandomTransactionInputWith(2, "minerKey", balance)));
    transaction.getOutputs().add(mapToTransactionOutput(createRandomTransactionOutputWith(3, "minerKey", balance)));
    return transaction;
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
      TransactionInput input3 = new TransactionInput();
      input3.setAmount(new BigDecimal("400"));
      input3.setPublicKey("minerKey");
      input3.setSequenceNumber(2l);
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
      TransactionOutput out3 = new TransactionOutput();
      out3.setAmount(new BigDecimal("399"));
      out3.setPublicKey("minerKey");
      out3.setSequenceNumber(2l);
      out3.setTimestamp(new Date().getTime() - 50l);
      outputs.add(out3);
    }
    TransactionOutput out4 = new TransactionOutput(); //change
    out4.setAmount(new BigDecimal("12"));
    out4.setPublicKey("minerKey");
    out4.setSequenceNumber(onlyReward ? 2l : 3l);
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
  
  public static List<SyncResponse<Block>> getFakeResponse(boolean isLast, long startPos, List<Block> blocks) {
    var response = new SyncResponse<Block>();
    response.setEntities(blocks);
    response.setLastPositionReached(isLast);
    response.setStartingPosition(startPos);
    return ListUtils.emptyIfNull(ImmutableList.of(response));
  }
  
  public static BlockInfo mapToInfo(Block block) {
    var info = new BlockInfo();
    info.setBlockHash(block.getHash());
    info.setPosition(block.getPosition());
    return info;
  }
  
  public static List<String> generateNodeIds(String prefix, int amount) {
    List<String> nodes = new ArrayList<>();
    IntStream.range(0, amount).forEach(a -> nodes.add(prefix + Integer.toString(a)));
    return nodes;
  }
  
  
  public static List<BlockInfo> fakeBlockInfos(int amount, int startPos, String prefix) {
    List<BlockInfo> infos = new ArrayList<>();
    LongStream.range(1, amount + 1).forEach(it -> {
      var info = new BlockInfo();
      info.setBlockHash(prefix + startPos + it);
      info.setPosition(startPos + it - 1);
      infos.add(info);
    });
    return infos;
  }
  
  public static List<Block> fakeBlocks(int amount, int startPos, String prefix) {
    List<Block> blocks = new ArrayList<>();
    LongStream.range(1, amount + 1).forEach(it -> {
      var block = new Block();
      block.setHash(prefix + startPos  + it);
      block.setPosition(startPos + it - 1);
      blocks.add(block);
    });
    return blocks;
  }
  
  public static List<BlockInfo> fakeBlockInfos(int amount, int startPos) {
    return fakeBlockInfos(amount, startPos, "hash");
  }
  
  public static List<Block> fakeBlocks(int amount, int startPos) {
    return fakeBlocks(amount, startPos, "hash");
  }
}
