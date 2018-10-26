package com.flockinger.groschn.blockchain.repository.model;

import java.util.Date;
import java.util.List;

public class StoredTransaction {
    
  private Date lockTime = null;

  private List<StoredTransactionInput> inputs = null;
  private List<StoredTransactionOutput> outputs = null;
  
  private String transactionHash = null;

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }
  
  public Date getLockTime() {
    return lockTime;
  }
  public void setLockTime(Date lockTime) {
    this.lockTime = lockTime;
  }
  public List<StoredTransactionInput> getInputs() {
    return inputs;
  }
  public void setInputs(List<StoredTransactionInput> inputs) {
    this.inputs = inputs;
  }
  public List<StoredTransactionOutput> getOutputs() {
    return outputs;
  }
  public void setOutputs(List<StoredTransactionOutput> outputs) {
    this.outputs = outputs;
  }
}
