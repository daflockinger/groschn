package com.flockinger.groschn.blockchain.consensus.model;

public class ProofOfMajorityConsent implements Consent {
  
  /**
   * Minimum amount of nodes to have a secure PoM consensus.
   */
  public final static Long MIN_ACTIVE_NODE_COUNT = 100l;
  
  private final static Long SEVEN_DAYS_IN_MILLISECONDS = 7l * 24l * 60l * 60l * 1000l;
  /**
   * Minimum amount of PoW blocks needed to be mined before switch to PoM.
   */
  public final static Long MIN_BLOCK_COUNT_BEFORE_ACTIVATE_POM = SEVEN_DAYS_IN_MILLISECONDS / PowConsent.MINING_RATE_MILLISECONDS;

  // TODO create that

  @Override
  public ConsensusType getType() {
    return ConsensusType.PROOF_OF_MAJORITY;
  }
}
