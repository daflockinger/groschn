package com.flockinger.groschn.blockchain.blockworks.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.dto.TransactionStatementDto;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.validation.transaction.TransactionException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionUtils;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.outbound.Broadcaster;

@Component
public class BlockMakerImpl implements BlockMaker {

  @Autowired
  private ConsensusFactory consensusFactory;
  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  private BlockStorageService storageService;
  @Autowired
  private CompressionUtils compressor;
  @Autowired
  private Bookkeeper bookkeeper;
  @Autowired
  private WalletService wallet;
  @Autowired
  private Broadcaster<CompressedEntity> broadcaster;
  @Autowired
  private ModelMapper mapper;

  private final static Logger LOG = LoggerFactory.getLogger(BlockMaker.class);

  @Override
  public void produceBlock() {
    List<Transaction> transactions =
        transactionManager.fetchTransactionsBySize(Block.MAX_TRANSACTION_BYTE_SIZE);
    try {
      addRewardTransaction(transactions);
      forgeBlockUnsafe(transactions);
    } catch (BlockchainException e) {
      LOG.error("Something went wrong while creating a new Block", e);
    }
  }

  private void forgeBlockUnsafe(List<Transaction> transactions) {
    Block generatedBlock = consensusFactory.reachConsensus(transactions);
    broadcastBlock(compressor.compress(generatedBlock));
    storageService.saveInBlockchain(generatedBlock);
  }

  private void broadcastBlock(CompressedEntity block) {
    Message<CompressedEntity> message = new Message<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(new Date().getTime());
    message.setPayload(block);
    broadcaster.broadcast(message, MainTopics.FRESH_BLOCK);
  }

  private void addRewardTransaction(List<Transaction> transactions) {
    String publicKey = wallet.getNodePublicKey();
    TransactionDto baseRewardTransaction = createBaseRewardTransaction(transactions, publicKey);
    TransactionDto rewardTransaction = new TransactionDto();
    Optional<Transaction> possibleTransaction = findExpenseTransaction(transactions, publicKey);
    if (possibleTransaction.isPresent()) {
      fixStatementsSequenceCount(baseRewardTransaction, possibleTransaction.get());
      rewardTransaction = mapper.map(possibleTransaction.get(), TransactionDto.class);
      transactions.remove(possibleTransaction.get());
    }
    rewardTransaction.getInputs().addAll(baseRewardTransaction.getInputs());
    rewardTransaction.getOutputs().addAll(baseRewardTransaction.getOutputs());
    rewardTransaction.setPublicKey(publicKey);
    transactions.add(transactionManager.createSignedTransaction(rewardTransaction));
  }

  private TransactionDto createBaseRewardTransaction(List<Transaction> transactions,
      String publicKey) {
    BigDecimal totalChange = transactions.stream().map(this::countTransactionSave)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    BigDecimal reward = bookkeeper.calculateBlockReward(storageService.getLatestBlock().getPosition());
    TransactionStatementDto rewardInput = createTransactionStatement(reward, 1l, publicKey);
    List<TransactionStatementDto> outputs = new ArrayList<>();
    outputs.add(createTransactionStatement(reward, 1l, publicKey));
    if (totalChange.compareTo(BigDecimal.ZERO) > 0) {
      outputs.add(createTransactionStatement(totalChange, 2l, publicKey));
    }
    TransactionDto transaction = new TransactionDto();
    transaction.getInputs().add(rewardInput);
    transaction.setOutputs(outputs);
    return transaction;
  }

  private BigDecimal countTransactionSave(Transaction transaction) {
    BigDecimal change = BigDecimal.ZERO;
    try {
      change = bookkeeper.countChange(transaction);
    } catch (TransactionException e) {
      LOG.warn("Skip transaction for change calculation cause it's invalid for it!", e);
    }
    return change;
  }

  private TransactionStatementDto createTransactionStatement(BigDecimal amount, long sequenceNumber,
      String publicKey) {
    TransactionStatementDto output = new TransactionStatementDto();
    output.setAmount(amount.doubleValue());
    output.setSequenceNumber(sequenceNumber);
    output.setPublicKey(publicKey);
    output.setTimestamp(new Date().getTime());
    return output;
  }

  private Optional<Transaction> findExpenseTransaction(List<Transaction> transactions,
      String publicKey) {
    TransactionUtils utils = TransactionUtils.build(publicKey);
    return utils.findLatestExpenseTransaction(transactions);
  }

  private void fixStatementsSequenceCount(TransactionDto rewardTransaction,
      Transaction existingTransaction) {
    Long lastInputSequence = existingTransaction.getInputs().stream()
        .map(TransactionInput::getSequenceNumber).reduce(Long::max).orElse(1l);
    Long lastOutputSequence = existingTransaction.getOutputs().stream()
        .map(TransactionOutput::getSequenceNumber).reduce(Long::max).orElse(1l);

    rewardTransaction.getInputs().get(0).setSequenceNumber(lastInputSequence + 1);
    for (TransactionStatementDto output : rewardTransaction.getOutputs()) {
      output.setSequenceNumber(++lastOutputSequence);
    }
  }
}

