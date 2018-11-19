package com.flockinger.groschn.blockchain.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.model.Block;

public class StartupConfig {

  @Bean
  @Primary
  public BlockStorageService storageService() {
    BlockStorageService service = mock(BlockStorageService.class);
    when(service.getLatestBlock()).thenReturn(Block.GENESIS_BLOCK());

    return service;
  }
}
