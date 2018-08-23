package com.flockinger.groschn.blockchain.dto;

import java.util.ArrayList;
import java.util.List;

public class TransactionDto {
  
  private List<TransactionStatementDto> inputs = new ArrayList<>();
  private List<TransactionStatementDto> outputs = new ArrayList<>();
  
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
