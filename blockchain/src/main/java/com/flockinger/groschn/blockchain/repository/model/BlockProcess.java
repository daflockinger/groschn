package com.flockinger.groschn.blockchain.repository.model;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;

@Document(collection="blockProcess")
public class BlockProcess {

  @Id
  private String id;
  
  @Field
  @Indexed(name="expireAtIdx", expireAfterSeconds= 7 * 24 * 60 * 60)
  private Date expireAt;
  
  private ConsensusType consensus;
  
  private Date startedAt;
  
  private Date finishedAt;
  
  private ProcessStatus status;

  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getExpireAt() {
    return expireAt;
  }

  public void setExpireAt(Date expireAt) {
    this.expireAt = expireAt;
  }

  public ConsensusType getConsensus() {
    return consensus;
  }

  public void setConsensus(ConsensusType consensus) {
    this.consensus = consensus;
  }

  public Date getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Date startedAt) {
    this.startedAt = startedAt;
  }

  public Date getFinishedAt() {
    return finishedAt;
  }

  public void setFinishedAt(Date finishedAt) {
    this.finishedAt = finishedAt;
  }

  public ProcessStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessStatus status) {
    this.status = status;
  }
}
