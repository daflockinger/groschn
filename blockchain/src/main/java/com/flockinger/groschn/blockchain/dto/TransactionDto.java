package com.flockinger.groschn.blockchain.dto;

import java.util.ArrayList;
import java.util.List;

public class TransactionDto {
  
  private String publicKey;
  private String secretWalletKey;
  
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
  public String getPublicKey() {
    return publicKey;
  }
  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }
  public String getSecretWalletKey() {
    return secretWalletKey;
  }
  public void setSecretWalletKey(String secretWalletKey) {
    this.secretWalletKey = secretWalletKey;
  }
}
