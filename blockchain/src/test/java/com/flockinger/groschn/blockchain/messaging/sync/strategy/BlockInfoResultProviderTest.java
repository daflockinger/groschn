package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;
import com.flockinger.groschn.blockchain.messaging.sync.impl.BlockChainSelector;
import com.flockinger.groschn.messaging.model.SyncBatchRequest;
import com.flockinger.groschn.messaging.sync.SyncInquirer;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockInfoResultProvider.class})
public class BlockInfoResultProviderTest {

  @MockBean
  private BlockChainSelector chainSelector;
  @MockBean
  private SyncInquirer inquirer;
  
  @Autowired
  private BlockInfoResultProvider provider;
  
  @Test
  public void testFetchBlockInfos_withValidFromPositionAndBatchSize_shouldFetchCorrectly() {
    var expectedBlockInfoResult = new BlockInfoResult(null, null);
    when(chainSelector.choose(isNotNull())).thenReturn(Optional.ofNullable(expectedBlockInfoResult));
    when(inquirer.fetchNextBatch(any(), any())).thenReturn(new ArrayList<>());
    
    
    var result = provider.fetchBlockInfos(3L, 20);
    
    verify(chainSelector).choose(any());
    var requestCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer).fetchNextBatch(requestCaptor.capture(), any());
    assertEquals("verify correct batch request batch-size", 20, requestCaptor.getValue().getBatchSize());
    assertEquals("verify correct batch request start position", 3L, requestCaptor.getValue().getFromPosition());
    
    assertTrue("verify that the result is present", result.isPresent());
    assertEquals("verify the correct BlockInfoResult is returned", expectedBlockInfoResult, result.get());
  }
  
  @Test
  public void testFetchBlockInfos_withTooLowFromPosition_shouldFetchFromPositionOne() {
    var expectedBlockInfoResult = new BlockInfoResult(null, null);
    when(chainSelector.choose(isNotNull())).thenReturn(Optional.ofNullable(expectedBlockInfoResult));
    when(inquirer.fetchNextBatch(any(), any())).thenReturn(new ArrayList<>());
    
    
    var result = provider.fetchBlockInfos(0L, 20);
    
    verify(chainSelector).choose(any());
    var requestCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer).fetchNextBatch(requestCaptor.capture(), any());
    assertEquals("verify correct batch request batch-size", 20, requestCaptor.getValue().getBatchSize());
    assertEquals("verify correct batch request start position", 1l, requestCaptor.getValue().getFromPosition());
    
    assertTrue("verify that the result is present", result.isPresent());
    assertEquals("verify the correct BlockInfoResult is returned", expectedBlockInfoResult, result.get());
  }
  
  @Test
  public void testFetchBlockInfos_withTooSmallBatchSize_shouldFetchAtLeastOne() {
    var expectedBlockInfoResult = new BlockInfoResult(null, null);
    when(chainSelector.choose(isNotNull())).thenReturn(Optional.ofNullable(expectedBlockInfoResult));
    when(inquirer.fetchNextBatch(any(), any())).thenReturn(new ArrayList<>());
    
    
    var result = provider.fetchBlockInfos(3L, 0);
    
    verify(chainSelector).choose(any());
    var requestCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer).fetchNextBatch(requestCaptor.capture(), any());
    assertEquals("verify correct batch request batch-size", 1, requestCaptor.getValue().getBatchSize());
    assertEquals("verify correct batch request start position", 3L, requestCaptor.getValue().getFromPosition());
    
    assertTrue("verify that the result is present", result.isPresent());
    assertEquals("verify the correct BlockInfoResult is returned", expectedBlockInfoResult, result.get());
  }
}
