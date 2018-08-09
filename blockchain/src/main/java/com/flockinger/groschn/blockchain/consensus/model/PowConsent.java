package com.flockinger.groschn.blockchain.consensus.model;

public class PowConsent implements Consent {
  private Long nonce;
  private Long timestamp;
  private Integer difficulty;
  private Long milliSecondsSpentMining;
  private ConsensusType type = ConsensusType.PROOF_OF_WORK;

  /**
   * Average time it should take to mine one block.
   */
  public final static Long MINING_RATE_MILLISECONDS = 30l * 1000l;
  
  /**
   * Default difficulty value for the very beginning.
   */
  public final static Integer DEFAULT_DIFFICULTY = 4;
  
  @Override
  public ConsensusType getType() {
    return type;
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
