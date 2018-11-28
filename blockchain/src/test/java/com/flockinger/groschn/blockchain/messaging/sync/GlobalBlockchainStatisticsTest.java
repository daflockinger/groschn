package com.flockinger.groschn.blockchain.messaging.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.messaging.dto.BlockInfo;
import com.flockinger.groschn.messaging.model.SyncResponse;
import com.flockinger.groschn.messaging.sync.SyncInquirer;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GlobalBlockchainStatistics.class})
public class GlobalBlockchainStatisticsTest {

  @MockBean
  private SyncInquirer inquirer;

  @Autowired
  private GlobalBlockchainStatistics statistics;

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

  private SyncResponse<BlockInfo> fakeResponse(Long lastPos) {
    var response = new SyncResponse<BlockInfo>();
    response.setLastPosition(lastPos);
    return response;
  }
}