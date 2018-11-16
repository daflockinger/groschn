package com.flockinger.groschn.blockchain.blockworks.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.RewardGenerator;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockGenerationStatus;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;
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
  private Broadcaster<MessagePayload> broadcaster;
  @Autowired
  private RewardGenerator rewardGenerator;

  @Value("${atomix.node-id}")
  private String nodeId;

  private volatile BlockGenerationStatus status = BlockGenerationStatus.COMPLETE;

  private final static Logger LOG = LoggerFactory.getLogger(BlockMaker.class);

  @Async
  @Override
  public void generation(BlockMakerCommand command) {
    switch (command) {
      case RESTART:
        status = BlockGenerationStatus.RUNNING;
        restart();
        break;
      case STOP:
        stop();
        status = BlockGenerationStatus.STOPPED;
        break;
      default:
        break;
    }
  }

  @Override
  public BlockGenerationStatus status() {
    return status;
  }

  private void restart() {
    stop();

    List<Transaction> transactions =
        transactionManager.fetchTransactionsBySize(Block.MAX_TRANSACTION_BYTE_SIZE);
    transactions = rewardGenerator.generateRewardTransaction(transactions);
    LOG.info("Restarting Block generation");;
    broadcastAndStore(transactions);
  }

  private void broadcastAndStore(List<Transaction> transactions) {
    try {
      var block = consensusFactory.reachConsensus(transactions);
      if (block.isPresent()) {
        broadcastBlock(block.get());
        storageService.saveInBlockchain(block.get());
      }
    } catch (BlockchainException e) {
      LOG.error("Cannot consent/send/store fresh Block cause: {}", e.getMessage());
    } finally {
      if(!BlockGenerationStatus.STOPPED.equals(status)) {
        status = BlockGenerationStatus.COMPLETE;
      }
    }
  }

  private void stop() {
    LOG.info("Stopping block generation!");
    consensusFactory.stopFindingConsensus();
  }

  private void broadcastBlock(Block block) {
    broadcaster.broadcast(block, nodeId, MainTopics.FRESH_BLOCK);
  }
}

