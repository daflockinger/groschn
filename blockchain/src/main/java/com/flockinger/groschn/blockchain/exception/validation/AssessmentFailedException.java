package com.flockinger.groschn.blockchain.exception.validation;

import com.flockinger.groschn.blockchain.validation.AssessmentFailure;

public class AssessmentFailedException extends ValidationException {
  /**
   * 
   */
  private static final long serialVersionUID = 8401202872486295245L;

  private final AssessmentFailure failure;
  
  public AssessmentFailedException(String message) {
    super(message);
    this.failure = AssessmentFailure.NONE;
  }
  
  public AssessmentFailedException(String message, AssessmentFailure failure) {
    super(message);
    this.failure = failure;
  }

  public AssessmentFailure getFailure() {
    return failure;
  }
}
