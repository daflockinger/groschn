package com.flockinger.groschn.blockchain.model;

import java.util.List;

public class Transaction implements Hashable {
  /**
  * 
  */
  private static final long serialVersionUID = -3848087917482658536L;

  private String id;

  /**
   * Timestamp when the transaction is done
   */
  private Long lockTime;

  private List<TransactionInput> inputs;
  private List<TransactionOutput> outputs;
  
  private String transactionHash;

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<TransactionInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<TransactionInput> inputs) {
    this.inputs = inputs;
  }

  public List<TransactionOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TransactionOutput> outputs) {
    this.outputs = outputs;
  }

  public Long getLockTime() {
    return lockTime;
  }

  public void setLockTime(Long lockTime) {
    this.lockTime = lockTime;
  }
}
