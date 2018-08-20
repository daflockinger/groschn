package com.flockinger.groschn.blockchain.blockworks;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockMakerImpl;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockMakerImpl.class})
public class BlockMakerTest {

  @MockBean
  private ConsensusFactory consensusFactory;
  @MockBean
  private TransactionManager transactionManager;
  @MockBean
  private BlockStorageService storageService;
  @MockBean
  private CompressionUtils compressor;
  @MockBean
  private Broadcaster<CompressedEntity> broadcaster;
  
  @Autowired
  private BlockMaker maker;
  
  @Test
  public void testProduceBlock_with_should() {
    when(consensusFactory.reachConsensus(anyList())).thenReturn(new Block());
    when(transactionManager.fetchTransactionsFromPool(anyLong())).thenReturn(ImmutableList.of(new Transaction()));
    when(compressor.compress(any())).thenReturn(CompressedEntity.build());
    
    maker.produceBlock();
    
    verify(transactionManager).fetchTransactionsFromPool(anyLong());
    verify(consensusFactory).reachConsensus(anyList());
    verify(compressor).compress(any());
    verify(broadcaster).broadcast(any());
  }
}
