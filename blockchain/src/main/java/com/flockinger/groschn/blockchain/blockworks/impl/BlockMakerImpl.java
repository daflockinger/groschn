package com.flockinger.groschn.blockchain.blockworks.impl;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.outbound.Broadcaster;

@Component
public class BlockMakerImpl implements BlockMaker {

  @Autowired
  private ConsensusFactory consensusFactory;
  @Autowired
  private TransactionManager transactionManager;
  
  private Broadcaster broadcaster;
  
  @Override
  public void produceBlock() {
    List<Transaction> transactions = transactionManager.fetchTransactionsFromPool(Block.MAX_TRANSACTION_BYTE_SIZE);
    Block generatedBlock = null;
    if(CollectionUtils.isNotEmpty(transactions)) {
      generatedBlock = consensusFactory.reachConsensus(transactions);
    }
    
    //TODO transform and compress that shit
    
  }

}
