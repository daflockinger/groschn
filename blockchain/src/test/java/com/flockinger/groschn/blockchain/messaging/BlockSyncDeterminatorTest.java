package com.flockinger.groschn.blockchain.messaging;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSyncDeterminator;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockSynchronizer;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockSyncDeterminator.class})
public class BlockSyncDeterminatorTest {
  
  @MockBean
  private BlockSynchronizer synchronizer;
  @MockBean
  private BlockStorageService blockService;
  @MockBean
  private SyncInquirer inquirer;
  
  @Autowired
  private BlockSyncDeterminator determinator;
  
  //TODO create many many tests to ensure it works!!
  @Test
  public void test_with_should() {
    
  }
}
