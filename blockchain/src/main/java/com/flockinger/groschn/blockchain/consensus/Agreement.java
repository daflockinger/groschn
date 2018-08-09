package com.flockinger.groschn.blockchain.consensus;

import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;

public class Agreement {
  
  private Consent consent;
  private Block block;
 
  public static Agreement build() {
    return new Agreement();
  }
  
  public Consent getConsent() {
    return consent;
  }
  public void setConsent(Consent consent) {
    this.consent = consent;
  }
  public Agreement consent(Consent consent) {
    this.consent = consent;
    return this;
  }
  public Block getBlock() {
    return block;
  }
  public void setBlock(Block block) {
    this.block = block;
  }
  public Agreement block(Block block) {
    this.block = block;
    return this;
  }
}
