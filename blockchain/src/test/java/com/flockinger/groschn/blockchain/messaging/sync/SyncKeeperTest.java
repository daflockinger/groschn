package com.flockinger.groschn.blockchain.messaging.sync;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SyncKeeper.class})
public class SyncKeeperTest {

  @MockBean
  private BlockStorageService blockMock;
  @MockBean
  private SmartBlockSynchronizer syncMock;
  @MockBean
  private GlobalBlockchainStatistics globalBlockchainStatistics;

  @Autowired
  private SyncKeeper syncKeeper;

  private final static long MAX_BLOCKCHAIN_POSITION_GAP = 3L;

  @Test
  public void testCheckSyncStatus_withNoGap_shouldNotSync() {
    var localFakeBlock = TestDataFactory.getFakeBlock();
    when(blockMock.getLatestBlock()).thenReturn(localFakeBlock);
    when(globalBlockchainStatistics.lastBlockPosition()).thenReturn(Optional.of(localFakeBlock.getPosition()));

    syncKeeper.checkSyncStatus();

    verify(syncMock, never()).sync(any());
  }

  @Test
  public void testCheckSyncStatus_withTooBigGap_shouldStartSync() {
    var localFakeBlock = TestDataFactory.getFakeBlock();
    when(blockMock.getLatestBlock()).thenReturn(localFakeBlock);
    var globalLastPosition = Optional.of(localFakeBlock.getPosition() + MAX_BLOCKCHAIN_POSITION_GAP + 1);
    when(globalBlockchainStatistics.lastBlockPosition()).thenReturn(globalLastPosition);

    syncKeeper.checkSyncStatus();

    verify(syncMock).sync(eq(globalLastPosition.get()));
  }

  @Test
  public void testCheckSyncStatus_withEmptyGlobalStatsReturned_shouldNotSync() {
    var localFakeBlock = TestDataFactory.getFakeBlock();
    when(blockMock.getLatestBlock()).thenReturn(localFakeBlock);
    when(globalBlockchainStatistics.lastBlockPosition()).thenReturn(Optional.empty());

    syncKeeper.checkSyncStatus();

    verify(syncMock,never()).sync(any());
  }

}