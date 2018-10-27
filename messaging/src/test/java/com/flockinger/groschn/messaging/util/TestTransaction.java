package com.flockinger.groschn.messaging.util;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import com.flockinger.groschn.blockchain.model.Hashable;

public class TestTransaction implements Hashable<TestTransaction> {
  /**
  * 
  */
  private static final long serialVersionUID = -3848087917482658536L;

  /**
   * Timestamp when the transaction is done
   */
  private Long lockTime;

  private List<TestTransactionInput> inputs;
  private List<TestTransactionOutput> outputs;
  
  private String transactionHash;

  public String getTransactionHash() {
    return transactionHash;
  }

  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }

  public List<TestTransactionInput> getInputs() {
    return inputs;
  }

  public void setInputs(List<TestTransactionInput> inputs) {
    this.inputs = inputs;
  }

  public List<TestTransactionOutput> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TestTransactionOutput> outputs) {
    this.outputs = outputs;
  }

  public Long getLockTime() {
    return lockTime;
  }

  public void setLockTime(Long lockTime) {
    this.lockTime = lockTime;
  }

  @Override
  public int compareTo(TestTransaction o) {
    if(o == null) {
      return 1;
    }
    return StringUtils.compare(this.getTransactionHash(), o.getTransactionHash());
  }

  @Override
  public String toString() {
    return "TestTransaction [lockTime=" + lockTime + ", inputs=" + inputs + ", outputs=" + outputs
        + ", transactionHash=" + transactionHash + "]";
  }
}
