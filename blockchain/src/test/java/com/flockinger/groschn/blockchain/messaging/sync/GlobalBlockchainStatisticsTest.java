package com.flockinger.groschn.blockchain.messaging.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;



public class GlobalBlockchainStatisticsTest {

  private SyncInquirer inquirer;
  private GlobalBlockchainStatistics statistics;

  @Before
  public void setup() {
    inquirer = mock(SyncInquirer.class);
    statistics = new GlobalBlockchainStatistics(inquirer);
  }

  @Test
  public void testLastBlockPosition_withManyDifferentLastPositions_shouldChooseMajoritysOne() {
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class)))
        .thenReturn(ImmutableList.of(fakeResponse(50L), fakeResponse(50L),
            fakeResponse(30L), fakeResponse(30L), fakeResponse(30L),
            fakeResponse(29L), fakeResponse(28L)));

    var lastPosition = statistics.lastBlockPosition();

    assertTrue("verify it returned a non empty last position", lastPosition.isPresent());
    assertEquals("verify correct majority last position", 30L, lastPosition.get().longValue());
  }

  @Test
  public void testLastBlockPosition_withTwoEquallyMajorPositions_shouldChooseEitherOneOne() {
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class)))
        .thenReturn(ImmutableList.of(fakeResponse(50L), fakeResponse(50L),
            fakeResponse(30L), fakeResponse(30L),
            fakeResponse(29L), fakeResponse(28L)));

    var lastPosition = statistics.lastBlockPosition();

    assertTrue("verify it returned a non empty last position", lastPosition.isPresent());
    assertTrue("verify correct one of majority last positions", lastPosition.get().longValue() == 30 || lastPosition.get().longValue() == 50);
  }

  @Test
  public void testLastBlockPosition_withInquirerReturnedEmpty_shouldReturnEmpty() {
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class))).thenReturn(new ArrayList<>());

    var lastPosition = statistics.lastBlockPosition();

    assertFalse("verify it returned empty", lastPosition.isPresent());
  }

  @Test
  public void testLastBlockPosition_withContainingNullLastPositions_shouldStillChooseCorrectly() {
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class)))
        .thenReturn(ImmutableList.of(fakeResponse(50L), fakeResponse(null),
            fakeResponse(30L), fakeResponse(30L),
            fakeResponse(29L), fakeResponse(null)));

    var lastPosition = statistics.lastBlockPosition();

    assertTrue("verify it returned a non empty last position", lastPosition.isPresent());
    assertTrue("verify correct one of majority last positions", lastPosition.get().longValue() == 30 || lastPosition.get().longValue() == 50);
  }


  @Test
  public void testOverallBlockHashes_withAllValidResponsesOneMajorHash_shouldReturnCorrect() {
    var responses = ImmutableList.of(fakeResponse("hash1", 27L, false),
        fakeResponse("hash1", 27L, false),
        fakeResponse("hash2", 27L, false),
        fakeResponse("hash1", 27L, false),
        fakeResponse("hash2", 27L, false));
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class))).thenReturn(responses);

    var hashes = statistics.overallBlockHashes(27L);

    assertNotNull("verify hashes returned not null", hashes);
    assertEquals("verify correct each node responded only one hash", 5, hashes.size());
    var hashCounts = hashes.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    assertEquals("verify correct first hash count", 3L, hashCounts.get("hash1").longValue());
    assertEquals("verify correct first hash count", 2L, hashCounts.get("hash2").longValue());
  }

  @Test
  public void testOverallBlockHashes_withSomeMultiHashResponsesHackyStuff_shouldStillReturnCorrect() {
    var responses = ImmutableList.of(fakeResponse("hash1", 27L, false),
        fakeResponse("hash1", 27L, false),
        fakeResponse("hash2", 27L, true),
        fakeResponse("hash1", 27L, false),
        fakeResponse("hash2", 27L, true));
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class))).thenReturn(responses);

    var hashes = statistics.overallBlockHashes(27L);

    assertNotNull("verify hashes returned not null", hashes);
    assertEquals("verify correct each node responded only one hash", 5, hashes.size());
    var hashCounts = hashes.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    assertEquals("verify correct first hash count", 3L, hashCounts.get("hash1").longValue());
    assertEquals("verify correct first hash count", 2L, hashCounts.get("hash2").longValue());
  }

  @Test
  public void testOverallBlockHashes_withSomeWrongPositionResponses_shouldStillReturnCorrect() {
    var responses = ImmutableList.of(fakeResponse("hash1", 27L, false),
        fakeResponse("hash1", 28L, false),
        fakeResponse("hash2", 27L, false),
        fakeResponse("hash1", 26L, false),
        fakeResponse("hash2", 27L, false));
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class))).thenReturn(responses);

    var hashes = statistics.overallBlockHashes(27L);

    assertNotNull("verify hashes returned not null", hashes);
    assertEquals("verify correct each node responded only one hash", 3, hashes.size());
    var hashCounts = hashes.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    assertEquals("verify correct first hash count", 1L, hashCounts.get("hash1").longValue());
    assertEquals("verify correct first hash count", 2L, hashCounts.get("hash2").longValue());
  }

  @Test
  public void testOverallBlockHashes_withOneNullHash_shouldStillReturnCorrect() {
    var responses = ImmutableList.of(fakeResponse("hash1", 27L, false),
        fakeResponse("hash1", 27L, false),
        fakeResponse("hash2", 27L, false),
        fakeResponse(null, 27L, false),
        fakeResponse("hash2", 27L, false));
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class))).thenReturn(responses);

    var hashes = statistics.overallBlockHashes(27L);

    assertNotNull("verify hashes returned not null", hashes);
    assertEquals("verify correct each node responded only one hash", 4, hashes.size());
    var hashCounts = hashes.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    assertEquals("verify correct first hash count", 2L, hashCounts.get("hash1").longValue());
    assertEquals("verify correct first hash count", 2L, hashCounts.get("hash2").longValue());
  }

  @Test
  public void testOverallBlockHashes_withOneEmptyResponse_shouldStillReturnCorrect() {
    var emptyResponse = new SyncResponse<BlockInfo>();
    emptyResponse.setEntities(new ArrayList<>());
    var responses = ImmutableList.of(fakeResponse("hash1", 27L, false),
        fakeResponse("hash1", 27L, false),
        fakeResponse("hash2", 27L, false),
        emptyResponse,
        fakeResponse("hash2", 27L, false));
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class))).thenReturn(responses);

    var hashes = statistics.overallBlockHashes(27L);

    assertNotNull("verify hashes returned not null", hashes);
    assertEquals("verify correct each node responded only one hash", 4, hashes.size());
    var hashCounts = hashes.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    assertEquals("verify correct first hash count", 2L, hashCounts.get("hash1").longValue());
    assertEquals("verify correct first hash count", 2L, hashCounts.get("hash2").longValue());
  }

  @Test
  public void testOverallBlockHashes_withNoResponsesWhatsoever_shouldReturnEmpty() {
    when(inquirer.fetchNextBatch(any(),eq(BlockInfo.class))).thenReturn(new ArrayList<>());

    var hashes = statistics.overallBlockHashes(27L);

    assertNotNull("verify hashes returned not null", hashes);
    assertTrue("verify that it returns empty with no responses", hashes.isEmpty());
  }


  private SyncResponse<BlockInfo> fakeResponse(String hash, Long position, boolean multi) {
      var response = new SyncResponse<BlockInfo>();
      var blockInfo = new BlockInfo();
      blockInfo.setBlockHash(hash);
      blockInfo.setPosition(position);
      var infos = new ArrayList<BlockInfo>();
      infos.add(blockInfo);
      if (multi) {
        var info2 = new BlockInfo();
        info2.setBlockHash(hash);
        info2.setPosition(position);
        var info3 = new BlockInfo();
        info3.setBlockHash(hash);
        info3.setPosition(position + 1);
        infos.add(info2);
        infos.add(info3);
      }
      response.setEntities(infos);
      return response;
  }

  private SyncResponse<BlockInfo> fakeResponse(Long lastPos) {
    var response = new SyncResponse<BlockInfo>();
    response.setLastPosition(lastPos);
    return response;
  }
}