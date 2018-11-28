package com.flockinger.groschn.blockchain.messaging.dto;

public enum SyncStatus {
   IN_PROGRESS, DONE;
  
  public final static String SYNC_STATUS_CACHE_KEY = "__BLOCK_SYNC_STATUS";
}
