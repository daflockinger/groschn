package com.flockinger.groschn.blockchain.repository.model;

import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="transactionPool")
public class StoredPoolTransaction {
  
  public final static String TX_HASH_NAME = "transactionHash";
  public final static String STATUS_NAME = "status";
  
  @Id
  private String id;
  
  @Field(TX_HASH_NAME)
  private String transactionHash;
  
  private Date lockTime;
  
  private Date createdAt;
  
  @Field(STATUS_NAME)
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
