package com.flockinger.groschn.blockchain.validation;

public class Assessment {
  
  private boolean isValid;
  String reasonOfFailure;
  
  public boolean isValid() {
    return isValid;
  }
  public void setValid(boolean isValid) {
    this.isValid = isValid;
  }
  public String getReasonOfFailure() {
    return reasonOfFailure;
  }
  public void setReasonOfFailure(String reasonOfFailure) {
    this.reasonOfFailure = reasonOfFailure;
  }
}
