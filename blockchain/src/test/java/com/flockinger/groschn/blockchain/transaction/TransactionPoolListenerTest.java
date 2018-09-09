package com.flockinger.groschn.blockchain.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.config.CacheConfig;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.messaging.MessageReceiverUtils;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionPoolListener;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.SubscriptionService;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TransactionPoolListener.class}, 
  initializers=ConfigFileApplicationContextInitializer.class)
@Import(CacheConfig.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class, ResetMocksTestExecutionListener.class})
public class TransactionPoolListenerTest {

  @MockBean
  private MessageReceiverUtils mockUtils;
  @MockBean
  private CompressionUtils compressor;
  @MockBean
  private SubscriptionService<MessagePayload> subscriptionService;
  @MockBean(reset=MockReset.BEFORE)
  private TransactionManager transactionManager;
  
  @Autowired
  @Qualifier("TransactionId_Cache")
  private Cache<String, String> transactionIdCache;
  
  @Autowired
  private TransactionPoolListener listener;
  
  private Transaction freshTransaction;
  
  @Before
  public void setup() {
    transactionIdCache.cleanUp();
    freshTransaction = TestDataFactory.createValidTransaction("ex1", "ex2", "ex3", "ex1");
    when(mockUtils.extractPayload(any(), any())).thenReturn(Optional.ofNullable(freshTransaction));
  }
    
  @Test
  public void testReceiveMessage_withValidBlockAndData_shouldStore() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.ofNullable(freshTransaction));
    Message<MessagePayload> message = validMessage();
    
    listener.receiveMessage(message);
    
    ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionManager).storeTransaction(txCaptor.capture());
    Transaction transactionToStore = txCaptor.getValue();
    assertNotNull("verify that to store transaction is not null", transactionToStore);
    assertEquals("verify that the to stored transaction is exactly the decompressed one", freshTransaction, transactionToStore);
  }
  
  @Test
  public void testReceiveMessage_withInvalidMessage_shouldDoNothing() {
    doThrow(ReceivedMessageInvalidException.class).when(mockUtils).assertMessage(any());
    Message<MessagePayload> message = validMessage();
    
    listener.receiveMessage(message);
    
    verify(transactionManager,times(0)).storeTransaction(any());
  }
  
  
  @Test
  public void testReceiveMessage_withFillingUpCacheTooMuch_shouldStillWork() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.ofNullable(freshTransaction));
    Message<MessagePayload> message = validMessage();
    
    for(int i=0; i < 30; i++) {
      message.setId(UUID.randomUUID().toString());
      listener.receiveMessage(message);
    }
    ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
    verify(transactionManager,times(30)).storeTransaction(txCaptor.capture());
    Transaction toStoreTransaction = txCaptor.getValue();
    assertNotNull("verify that to store transaction is not null", toStoreTransaction);
    assertEquals("verify that the to stored transaction is exactly the decompressed one", freshTransaction, toStoreTransaction);
  }
  
  @Test
  public void testReceiveMessage_withSendSameMessageTripple_shouldStoreOnlyOnce() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.ofNullable(freshTransaction));
    Message<MessagePayload> message = validMessage();
    
    message.setId("masterID");
    listener.receiveMessage(message);
    for(int i=0; i < 15; i++) {
      message.setId(UUID.randomUUID().toString());
      listener.receiveMessage(message);
    }
    message.setId("masterID");
    listener.receiveMessage(message);
    message.setId("masterID");
    listener.receiveMessage(message);
    
    verify(transactionManager,times(16)).storeTransaction(any());
  }
  
  
  @Test
  public void testReceiveMessage_withStorageValidationFailed_shouldDoNothing() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.of(freshTransaction));
    Message<MessagePayload> message = validMessage();
    doThrow(AssessmentFailedException.class).when(transactionManager).storeTransaction(any());
    
    listener.receiveMessage(message);
  }
  
  @Test
  public void testGetSubscribedTopic_shouldBeForTransactions() {
    assertEquals("verify that the fresh transaction topic is selected", 
        MainTopics.FRESH_TRANSACTION.name(), listener.getSubscribedTopic());
  }
  
  private Message<MessagePayload> validMessage() {
    Message<MessagePayload> message = new Message<>();
    message.setId(UUID.randomUUID().toString());
    message.setTimestamp(1000l);
    MessagePayload txMessage = new MessagePayload();
    CompressedEntity entity = CompressedEntity.build().originalSize(123).entity(new byte[10]);
    txMessage.setEntity(entity);
    txMessage.setSenderId(UUID.randomUUID().toString());
    message.setPayload(txMessage);
    return message;
  }
}