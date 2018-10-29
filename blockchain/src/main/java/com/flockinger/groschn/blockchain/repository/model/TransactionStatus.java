package com.flockinger.groschn.blockchain.repository.model;

public enum TransactionStatus {
  RAW("Fresh transactions queued in pool, not yet in the blockchain."), 
  EMBEDDED_IN_BLOCK("Transaction just embedded in a block in the blockchain."), 
  SIX_BLOCKS_UNDER("Transaction safely deep embedded in blochckain.");
  
  private String description;
  
  private TransactionStatus(String description) {
    this.description = description;
  }
  
  public String description() {
    return description;
  }
}
