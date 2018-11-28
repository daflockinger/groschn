package com.flockinger.groschn.blockchain.messaging.sync.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.blockchain.messaging.dto.BlockInfoResponse;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.stream.LongStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockChainSelector.class})
public class BlockChainSelectorTest {

  @Autowired
  private BlockChainSelector selector;
  
  @Test
  public void testChoose_withTwoChainsOneMajorlyUsed_shouldChooseMajorlyUsedOne() {
    var results = new ArrayList<BlockInfoResponse>();
    var minorDifference = createResponse(0,"minor", 10);
    minorDifference.getBlockInfos().get(9).setBlockHash("hashmajor10");
    results.addAll(ImmutableList.of(minorDifference, minorDifference));
    results.addAll(ImmutableList.of(createResponse(0,"major", 10), createResponse(1,"major", 10), createResponse(null, 2,"major", 10)));
    
    
    var chosenOne = selector.choose(results);
    
    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 2, chosenOne.get().getNodeIds().size());
    assertTrue("verify that there's no null node ID's", chosenOne.get().getNodeIds().stream().allMatch(id -> id != null));
    assertEquals("verify correct chosen node count", 10, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "major0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen second node ID", "major1", chosenOne.get().getNodeIds().get(1));
    assertEquals("verify correct chosen first info hash", "hashmajor0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertTrue("verify correct hashes are in there", chosenOne.get().getBlockInfos().stream().allMatch(info -> info.getBlockHash().startsWith("hashmajor")));
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
  }

  @Test
  public void testChoose_withTwoChainsOneHavingOnlyOneItemMatchingOtherButOneLonger_shouldChooseMajorlyUsedOne() {
    var results = new ArrayList<BlockInfoResponse>();
    results.add(createResponse(0,"major", 1));
    results.add(createResponse(1,"major", 10));


    var chosenOne = selector.choose(results);

    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 2, chosenOne.get().getNodeIds().size());
    assertTrue("verify that there's no null node ID's", chosenOne.get().getNodeIds().stream().allMatch(id -> id != null));
    assertEquals("verify correct chosen node count", 10, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "major0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen second node ID", "major1", chosenOne.get().getNodeIds().get(1));
    assertEquals("verify correct chosen first info hash", "hashmajor0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertTrue("verify correct hashes are in there", chosenOne.get().getBlockInfos().stream().allMatch(info -> info.getBlockHash().startsWith("hashmajor")));
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
  }
  
  
  
  @Test
  public void testChoose_withThreeChainsOneLongestButFewNodesOtherOneMajorlyUsed_shouldChooseMajorlyUsedOne() {
    var results = new ArrayList<BlockInfoResponse>();
    results.addAll(ImmutableList.of(createResponse(0,"major", 10), createResponse(1,"major", 10), createResponse(2,"major", 10),  createResponse(3,"major", 10)));
    results.addAll(ImmutableList.of(createResponse(0,"longest", 20), createResponse(1,"longest", 20), createResponse(2,"longest", 20)));
    results.addAll(ImmutableList.of(createResponse(0,"minor", 10), createResponse(1,"minor", 10)));
    
    var chosenOne = selector.choose(results);
    
    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 4, chosenOne.get().getNodeIds().size());
    assertEquals("verify correct chosen node count", 10, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "major0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen first info hash", "hashmajor0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
  }
  
  
  @Test
  public void testChoose_withTwoChainsHavingEqualyPopularChains_shouldChooseLongestOne() {
    var results = new ArrayList<BlockInfoResponse>();
    results.addAll(ImmutableList.of(createResponse(0,"shorty", 19), createResponse(1,"shorty", 19), createResponse(2,"shorty", 19)));
    results.addAll(ImmutableList.of(createResponse(0,"longy", 20), createResponse(1,"longy", 20), createResponse(2,"longy", 20)));
    
    var chosenOne = selector.choose(results);
    
    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 3, chosenOne.get().getNodeIds().size());
    assertEquals("verify correct chosen node count", 20, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "longy0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen first info hash", "hashlongy0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
  }
  
  @Test //TODO think about if it's really smart to choose the longest or not?
  public void testChoose_withTwoMajorOneHavingDifferentLengths_shouldChooseMajorlyUsedOneLongestVariant() {
    var results = new ArrayList<BlockInfoResponse>();
    results.addAll(ImmutableList.of(createResponse(0,"longy", 20), createResponse(1,"longy", 20)));
    results.addAll(ImmutableList.of(createResponse(0,"diff", 19), createResponse(1,"diff", 20), createResponse(2,"diff", 21)));
    
    var chosenOne = selector.choose(results);
    
    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 3, chosenOne.get().getNodeIds().size());
    assertEquals("verify correct chosen node count", 21, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "diff0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen first info hash", "hashdiff0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
    
    
    results = new ArrayList<BlockInfoResponse>();
    results.addAll(ImmutableList.of(createResponse(0,"diff", 19), createResponse(1,"diff", 20)));
    results.addAll(ImmutableList.of(createResponse(0,"longy", 20), createResponse(1,"longy", 20)));
    
    chosenOne = selector.choose(results);
    
    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 2, chosenOne.get().getNodeIds().size());
    assertEquals("verify correct chosen node count", 20, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "longy0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen second node ID", "longy1", chosenOne.get().getNodeIds().get(1));
    assertEquals("verify correct chosen first info hash", "hashlongy0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
    
  }
  
  
  @Test
  public void testChoose_withTwoAndMostPopularOneHavingGaps_shouldChooseOneWithoutGaps() {
    var results = new ArrayList<BlockInfoResponse>();
    results.addAll(ImmutableList.of(createResponseGapped(0,"gapped", 10), createResponseGapped(1,"gapped", 10), createResponseGapped(2,"gapped", 10)));
    results.addAll(ImmutableList.of(createResponse(0,"minor", 10), createResponse(1,"minor", 10)));
    
    var chosenOne = selector.choose(results);
    
    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 2, chosenOne.get().getNodeIds().size());
    assertEquals("verify correct chosen node count", 10, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "minor0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen first info hash", "hashminor0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
  }
  
  @Test
  public void testChoose_withTwoAndSomeOfMostPopularOneHavingGaps_shouldFilterGappedOnesOutAndDecide() {
    var results = new ArrayList<BlockInfoResponse>();
    results.addAll(ImmutableList.of(createResponse(0,"gapped", 10), createResponseGapped(1,"gapped", 10), createResponse(2,"gapped", 10)));
    results.addAll(ImmutableList.of(createResponse(0,"regular", 10), createResponse(1,"regular", 10), createResponse(2,"regular", 10)));
    
    var chosenOne = selector.choose(results);
    
    assertTrue("verify one was chosen", chosenOne.isPresent());
    assertEquals("verify correct chosen node count", 3, chosenOne.get().getNodeIds().size());
    assertEquals("verify correct chosen node count", 10, chosenOne.get().getBlockInfos().size());
    assertEquals("verify correct chosen first node ID", "regular0", chosenOne.get().getNodeIds().get(0));
    assertEquals("verify correct chosen first info hash", "hashregular0", chosenOne.get().getBlockInfos().get(0).getBlockHash());
    assertEquals("verify correct chosen first info position", 0, chosenOne.get().getBlockInfos().get(0).getPosition().longValue());
  }
  
  
  @Test
  public void testChoose_withEmptyInput_shouldReturnNotPresent() {
    var chosenOne = selector.choose(new ArrayList<>());
    
    assertFalse("verify none was chosen", chosenOne.isPresent());
  }
  
  @Test
  public void testChoose_withResponseHavingZeroNodes_shouldReturnNotPresent() {
    var results = new ArrayList<BlockInfoResponse>();
    results.addAll(ImmutableList.of(createResponse(0,"zero", 0), createResponse(2,"zero", 0)));
    
    var chosenOne = selector.choose(results);
    
    assertFalse("verify none was chosen", chosenOne.isPresent());
  }
  
  private BlockInfoResponse createResponseGapped(int number, String prefix, long chainSize) {
    var response = createResponse(number, prefix, chainSize);
    response.getBlockInfos().forEach(info -> {
      if(info.getPosition() % 2 == 0) {
        info.setPosition(info.getPosition() + 2);
      } else {
        info.setPosition(info.getPosition() + 1);
      }
      });
    return response;
  }
  
  private BlockInfoResponse createResponse(int number, String prefix, long chainSize) {
    return createResponse(prefix + number, number, prefix, chainSize);
  } 
  
  private BlockInfoResponse createResponse(String nodeId, int number, String prefix, long chainSize) {
    var infos = new ArrayList<BlockInfo>();
    LongStream.range(0, chainSize).forEach(count -> {
      var info = new BlockInfo();
      info.setPosition(count);
      info.setBlockHash("hash" + prefix + count);
      infos.add(info);
    });
    return new BlockInfoResponse(nodeId, infos);
  }
}
