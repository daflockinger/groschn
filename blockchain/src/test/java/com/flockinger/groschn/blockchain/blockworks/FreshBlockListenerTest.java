package com.flockinger.groschn.blockchain.blockworks;

import static com.flockinger.groschn.blockchain.TestDataFactory.getFakeBlock;
import static com.flockinger.groschn.blockchain.TestDataFactory.validMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;
import com.flockinger.groschn.blockchain.blockworks.impl.FreshBlockListener;
import com.flockinger.groschn.blockchain.messaging.sync.SmartBlockSynchronizer;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.AssessmentFailure;
import com.flockinger.groschn.blockchain.validation.impl.BlockValidator;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.inbound.MessagePackageHelper;
import com.flockinger.groschn.messaging.model.Message;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.test.context.ContextConfiguration;


@ContextConfiguration(classes = {FreshBlockListener.class})
@SuppressWarnings("unchecked")
public class FreshBlockListenerTest extends BaseCachingTest{

  @MockBean(reset=MockReset.BEFORE)
  private BlockStorageService blockService;
  @MockBean(name="blockValidator")
  private BlockValidator validator;
  @MockBean
  private BlockMaker blockMaker;
  @MockBean
  private SmartBlockSynchronizer smartSync;

  @MockBean
  private MessagePackageHelper helper;

  @MockBean
  @Qualifier("BlockId_Cache")
  private Cache<String, String> blockIdCache;
  
  @Autowired
  private FreshBlockListener listener;
  
  private Block freshBlock;
  
  @Before
  public void setup() {
    blockIdCache.cleanUp();
    freshBlock = getFakeBlock();
  }
    
  @Test
  public void testReceiveMessage_withValidBlockAndData_shouldStore() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.of(freshBlock));
    when(validator.validate(any())).thenReturn(Assessment.build().valid(true));
    Message<MessagePayload> message = validMessage();
    
    listener.receiveMessage(message);
    
    ArgumentCaptor<Block> blockCaptor = ArgumentCaptor.forClass(Block.class);
    verify(blockService).saveUnchecked(blockCaptor.capture());
    Block toStoreBlock = blockCaptor.getValue();
    assertNotNull("verify that to store block is not null", toStoreBlock);
    assertEquals("verify that the to stored block is exactly the decompressed one", freshBlock, toStoreBlock);
    verify(smartSync, times(0)).sync(any());
    
    verify(validator).validate(any());
    var commandCaptor = ArgumentCaptor.forClass(BlockMakerCommand.class);
    verify(blockMaker,times(2)).generation(commandCaptor.capture());
    assertTrue("verify it stoped and restarted block generation", 
        ImmutableList.of(BlockMakerCommand.STOP, BlockMakerCommand.RESTART).containsAll(
        commandCaptor.getAllValues()));
  }
  
  @Test
  public void testReceiveMessage_withInvalidMessage_shouldDoNothing() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.empty());
    Message<MessagePayload> message = validMessage();
    
    listener.receiveMessage(message);
    
    verify(blockService,times(0)).saveUnchecked(any());
    verify(smartSync, never()).sync(any());
  }
  
  
  @Test
  public void testReceiveMessage_withFillingUpCacheTooMuch_shouldStillWork() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.of(freshBlock));
    when(validator.validate(any())).thenReturn(Assessment.build().valid(true));
    Message<MessagePayload> message = validMessage();
    
    for(int i=0; i < 30; i++) {
      message.setId(UUID.randomUUID().toString());
      listener.receiveMessage(message);
    }
    ArgumentCaptor<Block> blockCaptor = ArgumentCaptor.forClass(Block.class);
    verify(blockService,times(30)).saveUnchecked(blockCaptor.capture());
    Block toStoreBlock = blockCaptor.getValue();
    assertNotNull("verify that to store block is not null", toStoreBlock);
    assertEquals("verify that the to stored block is exactly the decompressed one", freshBlock, toStoreBlock);
    verify(smartSync, never()).sync(any());
  }
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedMiserably_shouldDoNothing() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveUnchecked(any())).thenReturn(new StoredBlock());
    when(validator.validate(any())).thenReturn(Assessment.build().valid(false));
    
    listener.receiveMessage(message);
    
    verify(smartSync, never()).sync(any());
  }

  @Test
  public void testReceiveMessage_withStorageValidationFailedWithTooHighPosition_shouldResync() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveUnchecked(any())).thenReturn(new StoredBlock());
    when(validator.validate(any())).thenReturn(Assessment.build().valid(false)
        .reason("Block to far in the future").failure(AssessmentFailure.BLOCK_POSITION_TOO_HIGH));

    listener.receiveMessage(message);
    
    verify(smartSync, times(1)).sync(eq(97L));
  }
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedWithLastHashWrong_shouldResync() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveUnchecked(any())).thenReturn(new StoredBlock());
    when(validator.validate(any())).thenReturn(Assessment.build().valid(false)
        .reason("Block last hash wrong").failure(AssessmentFailure.BLOCK_LAST_HASH_WRONG));

    listener.receiveMessage(message);
    
    verify(smartSync, times(1)).sync(eq(freshBlock.getPosition()));
  }
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedSomewhereElse_shouldDoNothing() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveUnchecked(any())).thenReturn(new StoredBlock());
    when(validator.validate(any())).thenReturn(Assessment.build().valid(false)
        .reason("Something else was not right...").failure(AssessmentFailure.NONE));
    
    listener.receiveMessage(message);
    
    verify(smartSync, never()).sync(any());;
  }
  
  
  @Test
  public void testReceiveMessage_withStorageValidationFailedWithNoFailure_shouldDoNothing() {
    when(helper.verifyAndUnpackMessage(any(),any(),any(Class.class))).thenReturn(Optional.of(freshBlock));
    Message<MessagePayload> message = validMessage();
    when(blockService.saveUnchecked(any())).thenReturn(new StoredBlock());
    when(validator.validate(any())).thenReturn(Assessment.build().valid(false)
        .reason("Something else was not right..."));
    
    listener.receiveMessage(message);
    
    verify(smartSync, never()).sync(any());
  }

  @Test
  public void testGetSubscribedTopic_shouldBeForBlocks() {
    assertEquals("verify that the fresh block topic is selected", 
        MainTopics.FRESH_BLOCK, listener.getSubscribedTopic());
  }
}
