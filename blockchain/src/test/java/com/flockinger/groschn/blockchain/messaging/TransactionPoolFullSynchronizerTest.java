package com.flockinger.groschn.blockchain.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.exception.TransactionAlreadyClearedException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.messaging.dto.SyncBatchRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.SyncInquirer;
import com.flockinger.groschn.blockchain.messaging.sync.impl.TransactionPoolFullSynchronizer;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TransactionPoolFullSynchronizer.class})
@SuppressWarnings("unchecked")
public class TransactionPoolFullSynchronizerTest {
  
  @MockBean
  private TransactionManager transactionManager;
  @MockBean
  private SyncInquirer inquirer;
  
  @Autowired
  private TransactionPoolFullSynchronizer synchronizer;
  
  
  @Test
  public void testSynchronize_shouldDoFullSynchronization() {
    when(inquirer.fetchNextBatch(any(), any(Class.class))).thenReturn(getFakeResponse(false,1))
    .thenReturn(getFakeResponse(false,2)).thenReturn(getFakeResponse(false,3))
    .thenReturn(getFakeResponse(true,4));
    
    synchronizer.fullSynchronization();
    
    verify(transactionManager, times(380)).storeTransaction(any(Transaction.class));
    ArgumentCaptor<SyncBatchRequest> batchCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer, times(1 * 4)).fetchNextBatch(batchCaptor.capture(), any(Class.class));
    var batchRequests = batchCaptor.getAllValues();
    assertEquals("verify first requests batch size", 100, batchRequests.get(0).getBatchSize());
    assertEquals("verify first requests fetch retry count", 2, batchRequests.get(0).getMaxFetchRetries());
    assertEquals("verify first requests ideal node count", 3, batchRequests.get(0).getIdealReceiveNodeCount());
    assertEquals("verify first requests correct start position", 1l, batchRequests.get(0).getFromPosition());
    assertEquals("verify first requests correct target topic", MainTopics.SYNC_TRANSACTIONS, batchRequests.get(0).getTopic());
    assertEquals("verify second request start position", 2l, batchRequests.get(1).getFromPosition());
    assertEquals("verify third request start position", 3l, batchRequests.get(2).getFromPosition());
    assertEquals("verify fourth request start position", 4l, batchRequests.get(3).getFromPosition());
  }
  
  @Test
  public void testSynchronize_withEmptyEndResponse_shouldDoFullSynchronization() {
    when(inquirer.fetchNextBatch(any(), any(Class.class))).thenReturn(getFakeResponse(false,1))
    .thenReturn(getFakeResponse(false,2)).thenReturn(getFakeResponse(false,3))
    .thenReturn(getFakeResponse(false,4, new ArrayList<>()));
    
    synchronizer.fullSynchronization();
    
    verify(transactionManager, times(300)).storeTransaction(any(Transaction.class));
    ArgumentCaptor<SyncBatchRequest> batchCaptor = ArgumentCaptor.forClass(SyncBatchRequest.class);
    verify(inquirer, times(1 * 4)).fetchNextBatch(batchCaptor.capture(), any(Class.class));
    var batchRequests = batchCaptor.getAllValues();
    assertEquals("verify first requests batch size", 100, batchRequests.get(0).getBatchSize());
    assertEquals("verify first requests fetch retry count", 2, batchRequests.get(0).getMaxFetchRetries());
    assertEquals("verify first requests ideal node count", 3, batchRequests.get(0).getIdealReceiveNodeCount());
    assertEquals("verify first requests correct start position", 1l, batchRequests.get(0).getFromPosition());
    assertEquals("verify first requests correct target topic", MainTopics.SYNC_TRANSACTIONS, batchRequests.get(0).getTopic());
    assertEquals("verify second request start position", 2l, batchRequests.get(1).getFromPosition());
    assertEquals("verify third request start position", 3l, batchRequests.get(2).getFromPosition());
    assertEquals("verify fourth request start position", 4l, batchRequests.get(3).getFromPosition());
  }
  
  @Test
  public void testSynchronize_withNullEndResponse_shouldDoFullSynchronization() {
    when(inquirer.fetchNextBatch(any(), any(Class.class))).thenReturn(getFakeResponse(false,1))
    .thenReturn(getFakeResponse(false,2)).thenReturn(getFakeResponse(false,3))
    .thenReturn(getFakeResponse(false,4, null));
    
    synchronizer.fullSynchronization();
    
    verify(transactionManager, times(300)).storeTransaction(any(Transaction.class));
    verify(inquirer, times(1 * 4)).fetchNextBatch(any(), any(Class.class));
  }
  
  @Test
  public void testSynchronize_withTotallyEmptyLastResponse_shouldDoFullSynchronization() {
    when(inquirer.fetchNextBatch(any(), any(Class.class))).thenReturn(getFakeResponse(false,1))
    .thenReturn(getFakeResponse(false,2)).thenReturn(getFakeResponse(false,3))
    .thenReturn(Optional.empty());
    
    synchronizer.fullSynchronization();
    
    verify(transactionManager, times(300)).storeTransaction(any(Transaction.class));
    verify(inquirer, times(1 * 4)).fetchNextBatch(any(), any(Class.class));
  }
  
  @Test
  public void testSynchronize_withStorageReturningInvalid_shouldStopSyncing() {
    when(inquirer.fetchNextBatch(any(), any(Class.class))).thenReturn(getFakeResponse(false,1))
    .thenReturn(getFakeResponse(true, 2));;
    doNothing().doNothing().doThrow(AssessmentFailedException.class)
    .when(transactionManager).storeTransaction(any());
    
    synchronizer.fullSynchronization();
    
    verify(transactionManager, times(180)).storeTransaction(any(Transaction.class));
    verify(inquirer, times(2)).fetchNextBatch(any(), any(Class.class));
  }
  
  @Test
  public void testSynchronize_withStorageReturningAlreadyCleared_shouldStopSyncing() {
    when(inquirer.fetchNextBatch(any(), any(Class.class))).thenReturn(getFakeResponse(false,1))
    .thenReturn(getFakeResponse(true, 2));
    doNothing().doNothing().doThrow(TransactionAlreadyClearedException.class)
    .when(transactionManager).storeTransaction(any());
    
    synchronizer.fullSynchronization();
    
    verify(transactionManager, times(180)).storeTransaction(any(Transaction.class));
    verify(inquirer, times(2)).fetchNextBatch(any(), any(Class.class));
  }
  
  
  Optional<SyncResponse<Transaction>> getFakeResponse(boolean isLast, long startPos) {
    int txCount = isLast ? 80 : 100;
    var transactions = new ArrayList<Transaction>();
    for(int count = 0; count < txCount; count++) {
      transactions.add(new Transaction());
    }
    return getFakeResponse(isLast, startPos, transactions);
  }

  Optional<SyncResponse<Transaction>> getFakeResponse(boolean isLast, long startPos, List<Transaction> transactions) {
    var response = new SyncResponse<Transaction>();
    response.setEntities(transactions);
    response.setLastPositionReached(isLast);
    response.setStartingPosition(startPos);
    return Optional.ofNullable(response);
  }
}
