package com.flockinger.groschn.commons.model;

public enum TestSyncStatus {
  REQUEST_SYNC, IN_PROGRESS, DONE;
  
  public final static String SYNC_STATUS_CACHE_KEY = "__BLOCK_SYNC_STATUS";
}
