package com.flockinger.groschn.blockchain.messaging;

import static com.flockinger.groschn.blockchain.TestDataFactory.createBlockTransactions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.messaging.respond.TransactionFullSyncResponder;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.SyncRequest;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {TransactionFullSyncResponder.class})
public class TransactionFullSyncResponderTest extends BaseCachingTest {

  @MockBean
  private TransactionManager transactionManager;
  @MockBean
  @Qualifier("SyncTransactionId_Cache")
  private Cache<String, String> syncTransactionIdCache;

  @MockBean
  private MessagePackageHelper helper;
  
  @Autowired
  private TransactionFullSyncResponder responder;

  @Test
  public void testRespond_withValidRequestAndEnoughTransactions_shouldRespondNormal() {
    SyncRequest request = new SyncRequest();
    request.setStartingPosition(1l);
    request.setRequestPackageSize(100l);
    when(transactionManager.fetchTransactionsPaginated(anyInt(), anyInt())).thenReturn(createBlockTransactions(false, false));
    when(helper.verifyAndUnpackRequest(any(),any())).thenReturn(Optional.of(request));
    when(helper.packageResponse(any(SyncResponse.class), anyString())).thenReturn(new Message());

    var responseMessage = responder.respond(new Message<>());

    assertNotNull("verify response is not null", responseMessage);
    var responseCaptor = ArgumentCaptor.forClass(SyncResponse.class);
    verify(helper).packageResponse(responseCaptor.capture(),anyString());

    SyncResponse response = responseCaptor.getValue();
    assertEquals("verify that response starting position is correct", 1l,
        response.getStartingPosition().longValue());
    assertEquals("verify that response entity size is correct", 12l, response.getEntities().size());
    assertTrue("verify that response entity is correct class", Transaction.class.isInstance(response.getEntities().get(0)));
    assertEquals("verify that response is the last sync", true, response.isLastPositionReached());
    
    var pageCaptor = ArgumentCaptor.forClass(Integer.class);
    var sizeCaptor = ArgumentCaptor.forClass(Integer.class);
    verify(transactionManager).fetchTransactionsPaginated(pageCaptor.capture(), sizeCaptor.capture());
    assertEquals("verify that transactionManager is called with correct page", 0, pageCaptor.getValue().intValue());
    assertEquals("verify that transactionManager is called with correct page-size", 100, sizeCaptor.getValue().intValue());
  }

  @Test
  public void testGetSubscribedTopic_shouldReturnCorrect() {
    assertEquals("verify correct set topic", MainTopics.SYNC_TRANSACTIONS, responder.getSubscribedTopic());
  }
}
