package com.flockinger.groschn.blockchain.config;

import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.commons.BlockchainUtilsFactory;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.messaging.config.MessagingProtocolConfiguration;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlockchainMessagingProtocolConfiguration extends MessagingProtocolConfiguration {

  @Value("${blockchain.messaging.thread-pool.size}")
  private Integer threadPoolSize;

  @Override
  protected Compressor messageCompressor() {
    var registered = new ArrayList<Class<?>>();
    registered.add(Block.class);
    registered.add(Transaction.class);
    registered.add(TransactionInput.class);
    registered.add(TransactionOutput.class);
    registered.add(Consent.class);
    registered.add(ConsensusType.class);

    return BlockchainUtilsFactory.createCompressor(registered);
  }

  @Override
  protected Executor messageExecutor() {
    Executor executorService = Executors.newFixedThreadPool(threadPoolSize);
    return executorService;
  }
}
