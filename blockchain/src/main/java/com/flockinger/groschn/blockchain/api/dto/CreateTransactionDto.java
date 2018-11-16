package com.flockinger.groschn.blockchain.api.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * CreateTransactionDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class CreateTransactionDto   {
  @JsonProperty("inputs")
  @NotNull
  @Valid
  private List<TransactionInputDTO> inputs = null;

  @JsonProperty("outputs")
  @NotNull
  @Valid
  private List<TransactionOutputDTO> outputs = null;


  /**
   * Transaction inputs.
   * @return inputs
  **/
  @ApiModelProperty(value = "Transaction inputs.")
  @Valid
  public List<TransactionInputDTO> getInputs() {
    return inputs;
  }

  public void setInputs(List<TransactionInputDTO> inputs) {
    this.inputs = inputs;
  }

  /**
   * Transaction outputs.
   * @return outputs
  **/
  @ApiModelProperty(value = "Transaction outputs.")
  @Valid
  public List<TransactionOutputDTO> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TransactionOutputDTO> outputs) {
    this.outputs = outputs;
  }
}

