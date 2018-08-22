package com.flockinger.groschn.blockchain.dto;

import java.util.List;

public class TransactionDto {
  
  private List<TransactionStatementDto> inputs;
  private List<TransactionStatementDto> outputs;
  
  public List<TransactionStatementDto> getInputs() {
    return inputs;
  }
  public void setInputs(List<TransactionStatementDto> inputs) {
    this.inputs = inputs;
  }
  public List<TransactionStatementDto> getOutputs() {
    return outputs;
  }
  public void setOutputs(List<TransactionStatementDto> outputs) {
    this.outputs = outputs;
  }
}
