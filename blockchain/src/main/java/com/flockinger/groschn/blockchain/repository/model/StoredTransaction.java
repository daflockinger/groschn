package com.flockinger.groschn.blockchain.repository.model;

import java.util.Date;
import java.util.List;

public class StoredTransaction {
  
  private String transactionId;
  
  private Date lockTime;

  private List<StoredTransactionInput> inputs;
  private List<StoredTransactionOutput> outputs;
  
  private String transactionHash;

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }
  
  public String getTransactionId() {
    return transactionId;
  }
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
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
