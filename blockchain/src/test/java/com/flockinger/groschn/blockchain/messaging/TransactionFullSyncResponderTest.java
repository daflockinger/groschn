package com.flockinger.groschn.blockchain.messaging;

import static com.flockinger.groschn.blockchain.TestDataFactory.createBlockTransactions;
import static com.flockinger.groschn.blockchain.TestDataFactory.validMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncRequest;
import com.flockinger.groschn.blockchain.messaging.dto.SyncResponse;
import com.flockinger.groschn.blockchain.messaging.sync.impl.TransactionFullSyncResponder;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.serialize.impl.FstSerializer;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.model.CompressedEntity;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.ImmutableList;

@ContextConfiguration(classes = {TransactionFullSyncResponder.class, 
    CompressionUtils.class, MessagingUtils.class, FstSerializer.class})
public class TransactionFullSyncResponderTest extends BaseCachingTest {
  
  @MockBean
  private TransactionManager transactionManager;
  
  @Autowired
  private CompressionUtils compressor;
  @MockBean
  private NetworkStatistics networkStatistics;
  @Autowired
  @Qualifier("SyncTransactionId_Cache")
  private Cache<String, String> syncTransactionIdCache;
  
  @Autowired
  private TransactionFullSyncResponder responder;
  
  
  @Test
  public void testRespond_withValidRequestAndEnoughTransactions_shouldRespondNormal() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(1l);
    request.setRequestPackageSize(100l);
    message.getPayload().setEntity(compressor.compress(request));
    when(transactionManager.fetchTransactionsPaginated(anyInt(), anyInt())).thenReturn(createBlockTransactions(false, false));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    
    var responseMessage = responder.respond(message);
    
    assertTrue("verify response exists", responseMessage.isPresent());
    assertNotNull("verify response-message has a timestamp", responseMessage.get().getTimestamp());
    assertNotNull("verify response-message has a non null payload", responseMessage.get().getPayload());
    assertEquals("verify response-message has a valida sender id", "groschn-master-123", 
        responseMessage.get().getPayload().getSenderId());
    assertNotNull("verify response-message has an payload entity", responseMessage.get().getPayload().getEntity());
    CompressedEntity entity = responseMessage.get().getPayload().getEntity();
    assertTrue("verify that compressed entity is not empty", entity.getEntity().length > 0);
    assertTrue("verify that compressed entity has an original size", entity.getOriginalSize() > 0);
    
    var response = compressor.decompress(entity.getEntity(), entity.getOriginalSize(), SyncResponse.class);
    assertTrue("verify that response is there", response.isPresent());
    assertEquals("verify that response starting position is correct", 1l, 
        response.get().getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 12l, response.get().getEntities().size());
    assertTrue("verify that response entity is correct class", Transaction.class.isInstance(response.get().getEntities().get(0)));
    assertEquals("verify that response is the last sync", true, response.get().isLastPositionReached());
    
    var pageCaptor = ArgumentCaptor.forClass(Integer.class);
    var sizeCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(transactionManager).fetchTransactionsPaginated(pageCaptor.capture(), sizeCaptor.capture());
    assertEquals("verify that transactionManager is called with correct page", 0, pageCaptor.getValue().intValue());
    assertEquals("verify that transactionManager is called with correct page-size", 100, sizeCaptor.getValue().intValue());
  }
  
  @Test
  public void testRespond_withRequestWithoutStartingPoint_shouldRespondEmpty() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(null);
    request.setRequestPackageSize(100l);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(transactionManager.fetchTransactionsPaginated(anyInt(), anyInt())).thenReturn(createBlockTransactions(false, false));
    
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
  
  @Test
  public void testRespond_withRequestWithoutPackageSize_shouldRespondEmpty() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(2l);
    request.setRequestPackageSize(null);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(transactionManager.fetchTransactionsPaginated(anyInt(), anyInt())).thenReturn(createBlockTransactions(false, false));
    
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
  
  @Test
  public void testRespond_withRequestWithZeroStartingPoint_shouldRespondEmpty() {
    var message = validMessage();
    message.getPayload().setSenderId("pfennig-master");
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(0l);
    request.setRequestPackageSize(100l);
    message.getPayload().setEntity(compressor.compress(request));
    when(networkStatistics.activeNodeIds()).thenReturn(ImmutableList.of("groschn-master-123", "pfennig-master"));
    when(transactionManager.fetchTransactionsPaginated(anyInt(), anyInt())).thenReturn(createBlockTransactions(false, false));
    
    var responseMessage = responder.respond(message);
    
    assertFalse("verify response is empty", responseMessage.isPresent());
  }
}
