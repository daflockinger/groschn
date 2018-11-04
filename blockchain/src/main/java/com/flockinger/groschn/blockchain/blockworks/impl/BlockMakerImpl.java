package com.flockinger.groschn.blockchain.blockworks.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.RewardGenerator;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.commons.exception.BlockchainException;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;

@Component
public class BlockMakerImpl implements BlockMaker {

  @Autowired
  private ConsensusFactory consensusFactory;
  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  private BlockStorageService storageService;
  @Autowired
  private MessagingUtils messagingUtils;
  @Autowired
  private Broadcaster<MessagePayload> broadcaster;
  @Autowired
  private RewardGenerator rewardGenerator;
  
  @Value("${atomix.node-id}")
  private String nodeId;

  private final static Logger LOG = LoggerFactory.getLogger(BlockMaker.class);

  @Override
  public void produceBlock() {
    List<Transaction> transactions =
        transactionManager.fetchTransactionsBySize(Block.MAX_TRANSACTION_BYTE_SIZE);
    try {
      transactions = rewardGenerator.generateRewardTransaction(transactions);
      forgeBlockUnsafe(transactions);
    } catch (BlockchainException e) {
      LOG.error("Something went wrong while creating a new Block", e);
    }
  }

  private void forgeBlockUnsafe(List<Transaction> transactions) {
    Block generatedBlock = consensusFactory.reachConsensus(transactions);
    broadcastBlock(generatedBlock);
    storageService.saveInBlockchain(generatedBlock);
  }

  private void broadcastBlock(Block block) {
    var message = messagingUtils.packageMessage(block, nodeId);
    broadcaster.broadcast(message, MainTopics.FRESH_BLOCK);
    LOG.info("Freshly forged Block broadcasted with ID: " + message.getId());
  }
}

