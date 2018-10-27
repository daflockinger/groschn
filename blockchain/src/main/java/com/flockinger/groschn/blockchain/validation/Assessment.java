package com.flockinger.groschn.blockchain.validation;

public class Assessment {
  
  private boolean isValid;
  private String reasonOfFailure;
  private AssessmentFailure failure = AssessmentFailure.NONE;
  
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
  
  public static Assessment build() {
    return new Assessment();
  }
  
  public Assessment valid(boolean isValid) {
    setValid(isValid);
    return this;
  }
  
  public Assessment reason(String reasonOfFailure) {
    setReasonOfFailure(reasonOfFailure);
    return this;
  }
  
  public AssessmentFailure getFailure() {
    return failure;
  }
  
  public void setFailure(AssessmentFailure failure) {
    this.failure = failure;
  }
}
