package com.flockinger.groschn.blockchain.model;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class Transaction implements Hashable<Transaction> {
  /**
  * 
  */
  private static final long serialVersionUID = -3848087917482658536L;

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

  @Override
  public int compareTo(Transaction o) {
    if(o == null) {
      return 1;
    }
    return StringUtils.compare(this.getTransactionHash(), o.getTransactionHash());
  }
  
  
}
