package com.flockinger.groschn.blockchain.blockworks;

import static com.flockinger.groschn.blockchain.TestDataFactory.getFakeBlock;
import static com.flockinger.groschn.blockchain.TestDataFactory.validMessage;
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
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.blockworks.impl.FreshBlockListener;
import com.flockinger.groschn.blockchain.dto.MessagePayload;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.messaging.ReceivedMessageInvalidException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.messaging.sync.SyncDeterminator;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.validation.AssessmentFailure;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.Message;
import com.github.benmanes.caffeine.cache.Cache;


@ContextConfiguration(classes = {FreshBlockListener.class})
public class FreshBlockListenerTest extends BaseCachingTest{

  @MockBean
  private MessagingUtils mockUtils;
  @MockBean
  private CompressionUtils compressor;
  @MockBean(reset=MockReset.BEFORE)
  private BlockStorageService blockService;
  @MockBean
  private SyncDeterminator blockSyncDeterminator;
  
  @Autowired
  @Qualifier("BlockId_Cache")
  private Cache<String, String> blockIdCache;
  
  @Autowired
  private FreshBlockListener listener;
  
  private Block freshBlock;
  
  @Before
  public void setup() {
    blockIdCache.cleanUp();
    freshBlock = getFakeBlock();
    when(mockUtils.extractPayload(any(), any())).thenReturn(Optional.ofNullable(freshBlock));
  }
    
  @Test
  public void testReceiveMessage_withValidBlockAndData_shouldStore() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.ofNullable(freshBlock));
    Message<MessagePayload> message = validMessage();
    
    listener.receiveMessage(message);
    
    ArgumentCaptor<Block> blockCaptor = ArgumentCaptor.forClass(Block.class);
    verify(blockService).saveInBlockchain(blockCaptor.capture());
    Block toStoreBlock = blockCaptor.getValue();
    assertNotNull("verify that to store block is not null", toStoreBlock);
    assertEquals("verify that the to stored block is exactly the decompressed one", freshBlock, toStoreBlock);
    verify(blockSyncDeterminator, times(0)).determineAndSync();
  }
  
  @Test
  public void testReceiveMessage_withInvalidMessage_shouldDoNothing() {
    doThrow(ReceivedMessageInvalidException.class).when(mockUtils).assertEntity(any());
    Message<MessagePayload> message = validMessage();
    
    listener.receiveMessage(message);
    
    verify(blockService,times(0)).saveInBlockchain(any());
    verify(blockSyncDeterminator, times(0)).determineAndSync();
  }
  
  
  @Test
  public void testReceiveMessage_withFillingUpCacheTooMuch_shouldStillWork() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.ofNullable(freshBlock));
    Message<MessagePayload> message = validMessage();
    
    for(int i=0; i < 30; i++) {
      message.setId(UUID.randomUUID().toString());
      listener.receiveMessage(message);
    }
    ArgumentCaptor<Block> blockCaptor = ArgumentCaptor.forClass(Block.class);
    verify(blockService,times(30)).saveInBlockchain(blockCaptor.capture());
    Block toStoreBlock = blockCaptor.getValue();
    assertNotNull("verify that to store block is not null", toStoreBlock);
    assertEquals("verify that the to stored block is exactly the decompressed one", freshBlock, toStoreBlock);
    verify(blockSyncDeterminator, times(0)).determineAndSync();
  }
  
  @Test
  public void testReceiveMessage_withSendSameMessageTripple_shouldStoreOnlyOnce() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.ofNullable(freshBlock));
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
    
    ArgumentCaptor<Block> blockCaptor = ArgumentCaptor.forClass(Block.class);
    verify(blockService,times(16)).saveInBlockchain(blockCaptor.capture());
  }
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedMiserably_shouldDoNothing() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveInBlockchain(any())).thenThrow(BlockchainException.class);
    
    listener.receiveMessage(message);
    
    verify(blockSyncDeterminator, times(0)).determineAndSync();
  }
  
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedWithTooHighPosition_shouldResync() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveInBlockchain(any())).thenAnswer(new Answer<StoredBlock>() {
      @Override
      public StoredBlock answer(InvocationOnMock invocation) throws Throwable {
        throw new AssessmentFailedException("Block to far in the future", AssessmentFailure.BLOCK_POSITION_TOO_HIGH);
      }} );
    listener.receiveMessage(message);
    
    verify(blockSyncDeterminator, times(1)).determineAndSync();
  }
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedWithLastHashWrong_shouldResync() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveInBlockchain(any())).thenAnswer(new Answer<StoredBlock>() {
      @Override
      public StoredBlock answer(InvocationOnMock invocation) throws Throwable {
        throw new AssessmentFailedException("Block last hash wrong", AssessmentFailure.BLOCK_LAST_HASH_WRONG);
      }} );
    listener.receiveMessage(message);
    
    verify(blockSyncDeterminator, times(1)).determineAndSync();
  }
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedSomewhereElse_shouldDoNothing() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveInBlockchain(any())).thenAnswer(new Answer<StoredBlock>() {
      @Override
      public StoredBlock answer(InvocationOnMock invocation) throws Throwable {
        throw new AssessmentFailedException("Something else was not right...", AssessmentFailure.NONE);
      }} );
    listener.receiveMessage(message);
    
    verify(blockSyncDeterminator, times(0)).determineAndSync();
  }
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedWithNoFailure_shouldDoNothing() {
    when(compressor.decompress(any(), any(Integer.class), any())).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveInBlockchain(any())).thenAnswer(new Answer<StoredBlock>() {
      @Override
      public StoredBlock answer(InvocationOnMock invocation) throws Throwable {
        throw new AssessmentFailedException("Something else was not right...");
      }} );
    listener.receiveMessage(message);
    
    verify(blockSyncDeterminator, times(0)).determineAndSync();
  }
  
  
  
  @Test
  public void testGetSubscribedTopic_shouldBeForBlocks() {
    assertEquals("verify that the fresh block topic is selected", 
        MainTopics.FRESH_BLOCK, listener.getSubscribedTopic());
  }
}
