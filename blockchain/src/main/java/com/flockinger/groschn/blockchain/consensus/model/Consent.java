package com.flockinger.groschn.blockchain.consensus.model;

import com.flockinger.groschn.blockchain.model.Hashable;

public class Consent implements Hashable {
  /**
   * 
   */
  private static final long serialVersionUID = -8569663167682846480L;
  
  private Long nonce;
  private Long timestamp;
  private Integer difficulty;
  private Long milliSecondsSpentMining;
  private ConsensusType type;

  public ConsensusType getType() {
    return type;
  }

  public void setType(ConsensusType type) {
    this.type = type;
  }

  public Long getMilliSecondsSpentMining() {
    return milliSecondsSpentMining;
  }

  public void setMilliSecondsSpentMining(Long milliSecondsSpentMining) {
    this.milliSecondsSpentMining = milliSecondsSpentMining;
  }

  public Integer getDifficulty() {
    return difficulty;
  }

  public void setDifficulty(Integer difficulty) {
    this.difficulty = difficulty;
  }

  public Long getNonce() {
    return nonce;
  }

  public void setNonce(Long nonce) {
    this.nonce = nonce;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }
}
