package com.flockinger.groschn.blockchain.repository.model;

import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="transactionPool")
public class StoredPoolTransaction {
  
  @Id
  private String id;
  
  private String transactionId;
  
  private Date lockTime;
  
  private Date createdAt;
  
  private TransactionStatus status;

  private List<StoredTransactionInput> inputs;
  private List<StoredTransactionOutput> outputs;
  
  
  public Date getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
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
  public TransactionStatus getStatus() {
    return status;
  }
  public void setStatus(TransactionStatus status) {
    this.status = status;
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
