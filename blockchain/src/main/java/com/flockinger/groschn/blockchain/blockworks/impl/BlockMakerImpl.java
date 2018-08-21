package com.flockinger.groschn.blockchain.blockworks.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
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
  private Broadcaster<CompressedEntity> broadcaster;
  
  private final static Logger LOG = LoggerFactory.getLogger(BlockMaker.class);
  
  @Override
  public void produceBlock() {
    List<Transaction> transactions = transactionManager.fetchTransactionsFromPool(Block.MAX_TRANSACTION_BYTE_SIZE);
    try {
      forgeBlockUnsafe(transactions);
    } catch (BlockchainException e) {
      LOG.error("Something went wrong while creating a new Block", e);
    }
  }
  
  private void forgeBlockUnsafe(List<Transaction> transactions) {
    if(CollectionUtils.isNotEmpty(transactions)) {
      Block generatedBlock = consensusFactory.reachConsensus(transactions);
      broadcastBlock(compressor.compress(generatedBlock));
      storageService.saveInBlockchain(generatedBlock);
    }
  }
  
  private void broadcastBlock(CompressedEntity block) {
    Message<CompressedEntity> message = new Message<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(new Date().getTime());
    message.setPayload(block);
    broadcaster.broadcast(message);
  }
  
  private void addRewardTransaction(List<Transaction> transactions) {
    //TODO implement
  }
}
