package com.flockinger.groschn.blockchain.repository.model;

import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="blockchain")
public class StoredBlock {

  @Id
  private String id;
  
  @Indexed
  @NotNull
  private Long position;
  
  @Indexed
  @NotNull
  private String hash;
  
  @NotNull
  private String lastHash;
  
  @Indexed
  @NotNull
  private Long timestamp;
  
  private Integer version;
  
  //TODO 
  // added fixed version of Consents and Transactions
  // add all the indexes I need for every query (but not too much)

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getPosition() {
    return position;
  }

  public void setPosition(Long position) {
    this.position = position;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getLastHash() {
    return lastHash;
  }

  public void setLastHash(String lastHash) {
    this.lastHash = lastHash;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }
}
