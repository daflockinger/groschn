package com.flockinger.groschn.blockchain.messaging.dto;

public class SyncSettings {
  
  private long fromPos;
  private long toPos;
  private SyncStrategyType strategyType;
  
  private SyncSettings() {}
  
  public static SyncSettings scan(long fromPos) {
    var settings = new SyncSettings();
    settings.strategyType = SyncStrategyType.SCAN;
    settings.fromPos = fromPos;
    return settings;
  }

  public static SyncSettings confident(long overlappingFromPosition, long toPos) {
    var settings = new SyncSettings();
    settings.strategyType = SyncStrategyType.CONFIDENT;
    settings.fromPos = overlappingFromPosition;
    settings.toPos = toPos;
    return settings;
  }

  public long getFromPos() {
    return fromPos;
  }

  public long getToPos() {
    return toPos;
  }

  public SyncStrategyType getStrategyType() {
    return strategyType;
  }
}
