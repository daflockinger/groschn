package com.flockinger;

import com.flockinger.groschn.commons.BlockchainUtilsFactory;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.messaging.config.MessagingProtocolConfiguration;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestMessagingProtocolConfiguration extends MessagingProtocolConfiguration {

  @Override
  protected Compressor messageCompressor() {
    return BlockchainUtilsFactory.createCompressor(new ArrayList<>());
  }

  @Override
  protected Executor messageExecutor() {
    return Executors.newFixedThreadPool(50);
  }
}
