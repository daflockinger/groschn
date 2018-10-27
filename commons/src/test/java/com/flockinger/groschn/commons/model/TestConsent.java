package com.flockinger.groschn.commons.model;

import com.flockinger.groschn.blockchain.model.Hashable;

public class TestConsent implements Hashable<TestConsent> {
  /**
   * 
   */
  private static final long serialVersionUID = -8569663167682846480L;
  
  /**
   * Default difficulty value for the very beginning.
   */
  public final static Integer DEFAULT_DIFFICULTY = 4;

  private Long nonce;
  private Long timestamp;
  private Integer difficulty;
  private Long milliSecondsSpentMining;
  private TestConsensusType type;

  public TestConsensusType getType() {
    return type;
  }

  public void setType(TestConsensusType type) {
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

  @Override
  public int compareTo(TestConsent o) {
    if (this.getTimestamp() == null && o.getTimestamp() == null) {
      return 0;
    } else if (this.getTimestamp() == null) {
      return -1;
    } else if (o.getTimestamp() == null) {
      return 1;
    }
    return this.getTimestamp().compareTo(o.getTimestamp());
  }

  @Override
  public String toString() {
    return "TestConsent [nonce=" + nonce + ", timestamp=" + timestamp + ", difficulty=" + difficulty
        + ", milliSecondsSpentMining=" + milliSecondsSpentMining + ", type=" + type + "]";
  }
}
