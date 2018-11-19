package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import static com.flockinger.groschn.blockchain.TestDataFactory.fakeBlockInfos;
import static com.flockinger.groschn.blockchain.TestDataFactory.fakeBlocks;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResult;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ScanResultMatcher.class})
public class ScanResultMatcherTest {
  
  @MockBean
  private BlockStorageService blockService;
  
  @Autowired
  private ScanResultMatcher matcher;

  
  private ScanningContext context = ScanningContext.build(null).batchSize(10);
  
  @Test
  public void testFindMatchingStartPosition_withAllMatching_shouldReturnHighest() {
    var infos = fakeBlockInfos(10, 14);
    var blocks = fakeBlocks(10, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertTrue("verify that match was found", match.isPresent());
    assertEquals("verify match is correct", 23L, match.get().longValue());
  }
  
  @Test
  public void testFindMatchingStartPosition_withMultipleMatching_shouldReturnHighest() {
    var infos = fakeBlockInfos(10, 14);
    infos.get(9).setBlockHash("wrong" + 9);
    infos.get(8).setBlockHash("wrong" + 8);
    infos.get(7).setBlockHash("wrong" + 7);
    infos.get(6).setBlockHash("wrong" + 6);
    infos.get(5).setBlockHash("wrong" + 5);
    infos.get(4).setBlockHash("wrong" + 4);
    var blocks = fakeBlocks(10, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertTrue("verify that match was found", match.isPresent());
    assertEquals("verify match is correct", 17L, match.get().longValue());
  }
  
  @Test
  public void testFindMatchingStartPosition_withOneMatchingAtTheStart_shouldReturnThatOne() {
    var infos = fakeBlockInfos(10, 14);
    infos.forEach(it -> it.setBlockHash("BAM" + it.getBlockHash()));
    infos.get(0).setBlockHash("hash141");
    var blocks = fakeBlocks(10, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertTrue("verify that match was found", match.isPresent());
    assertEquals("verify match is correct", 14L, match.get().longValue());
  }
  
  @Test
  public void testFindMatchingStartPosition_withOneMatchingInTheMiddle_shouldReturnEmpty() {
    var infos = fakeBlockInfos(10, 14);
    infos.forEach(it -> it.setBlockHash("BAM" + it.getBlockHash()));
    infos.get(2).setBlockHash("hash3");
    var blocks = fakeBlocks(10, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertFalse("verify that match was not found, cause this only happens on corrupted blockchains", match.isPresent());
  }
  
  @Test
  public void testFindMatchingStartPosition_withAllButFirstOneMatching_shouldReturnEmpty() {
    var infos = fakeBlockInfos(10, 14);
    infos.get(0).setBlockHash("bam1");
    var blocks = fakeBlocks(10, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertFalse("verify that match was not found, cause this only happens on corrupted blockchains", match.isPresent());
  }
  
  @Test
  public void testFindMatchingStartPosition_withNoneMatching_shouldReturnEmpty() {
    var infos = fakeBlockInfos(10, 14);
    infos.forEach(it -> it.setBlockHash("BAM" + it.getBlockHash()));
    var blocks = fakeBlocks(10, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertFalse("verify that match was not found", match.isPresent());
  }
  
  
  @Test
  public void testFindMatchingStartPosition_withMissingReceivedInfos_shouldReturnHighestReceived() {
    var infos = fakeBlockInfos(8, 14);
    var blocks = fakeBlocks(10, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertTrue("verify that match was found", match.isPresent());
    assertEquals("verify match is correct", 21L, match.get().longValue());
  }
  
  @Test
  public void testFindMatchingStartPosition_withMissingStoredBlocks_shouldReturnHighestStored() {
    var infos = fakeBlockInfos(10, 14);
    var blocks = fakeBlocks(7, 14);
    Collections.shuffle(infos);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertTrue("verify that match was found", match.isPresent());
    assertEquals("verify match is correct", 20L, match.get().longValue());
  }
  
  @Test
  public void testFindMatchingStartPosition_withEmptyBlocks_shouldReturnEmpty() {
    var infos = fakeBlockInfos(8, 14);
    Collections.shuffle(infos);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), infos));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(new ArrayList<>());
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertFalse("verify that match was not found", match.isPresent());
  }
  
  @Test
  public void testFindMatchingStartPosition_withEmptyReceivedInfos_shouldReturnEmpty() {
    var blocks = fakeBlocks(7, 14);
    Collections.shuffle(blocks);
    var infoResult = Optional.of(new BlockInfoResult(new ArrayList<>(), new ArrayList<>()));
    when(blockService.findBlocks(anyLong(), anyLong())).thenReturn(blocks);
    
    var match = matcher.findMatchingStartPosition(context.currentResult(infoResult).fromPosition(14L));
    
    assertFalse("verify that match was not found", match.isPresent());
  }
    
  
  
  
}
