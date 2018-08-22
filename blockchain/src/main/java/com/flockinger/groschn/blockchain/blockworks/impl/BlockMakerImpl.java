package com.flockinger.groschn.blockchain.blockworks.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.google.common.collect.ImmutableList;

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

  private final static Logger LOG = LoggerFactory.getLogger(BlockMaker.class);

  @Override
  public void produceBlock() {
    List<Transaction> transactions =
        transactionManager.fetchTransactionsFromPool(Block.MAX_TRANSACTION_BYTE_SIZE);
    try {
      transactions.add(createRewardTransaction(transactions));
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
    broadcaster.broadcast(message);
  }

  private Transaction createRewardTransaction(List<Transaction> transactions) {
    BigDecimal totalChange = transactions.stream().map(this::countTransactionSave)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    BigDecimal reward = bookkeeper.calculateBlockReward(storageService.getLatestBlockPosition());
    String publicKey = wallet.getPublicKey();
    TransactionDto rewardTransaction = new TransactionDto();
    TransactionStatementDto rewardInput = createTransactionStatement(reward, 1l, publicKey);
    rewardTransaction.setInputs(ImmutableList.of(rewardInput));
    List<TransactionStatementDto> outputs = new ArrayList<>();
    outputs.add(createTransactionStatement(reward, 1l, publicKey));
    if (totalChange.compareTo(BigDecimal.ZERO) > 0) {
      outputs.add(createTransactionStatement(totalChange, 2l, publicKey));
    }
    rewardTransaction.setOutputs(outputs);
    return transactionManager.createSignedTransaction(rewardTransaction);
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

  private BigDecimal countTransactionSave(Transaction transaction) {
    BigDecimal change = BigDecimal.ZERO;
    try {
      change = bookkeeper.countChange(transaction);
    } catch (TransactionException e) {
      LOG.warn("Skip transaction for change calculation cause it's invalid for it!", e);
    }
    return change;
  }
}
