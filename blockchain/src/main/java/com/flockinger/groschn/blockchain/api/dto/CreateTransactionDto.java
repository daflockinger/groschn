package com.flockinger.groschn.blockchain.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
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
  @Valid
  private List<TransactionInputDTO> inputs = null;

  @JsonProperty("outputs")
  @Valid
  private List<TransactionOutputDTO> outputs = null;

  public CreateTransactionDto inputs(List<TransactionInputDTO> inputs) {
    this.inputs = inputs;
    return this;
  }

  public CreateTransactionDto addInputsItem(TransactionInputDTO inputsItem) {
    if (this.inputs == null) {
      this.inputs = new ArrayList<TransactionInputDTO>();
    }
    this.inputs.add(inputsItem);
    return this;
  }

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

  public CreateTransactionDto outputs(List<TransactionOutputDTO> outputs) {
    this.outputs = outputs;
    return this;
  }

  public CreateTransactionDto addOutputsItem(TransactionOutputDTO outputsItem) {
    if (this.outputs == null) {
      this.outputs = new ArrayList<TransactionOutputDTO>();
    }
    this.outputs.add(outputsItem);
    return this;
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateTransactionDto createTransactionDto = (CreateTransactionDto) o;
    return Objects.equals(this.inputs, createTransactionDto.inputs) &&
        Objects.equals(this.outputs, createTransactionDto.outputs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(inputs, outputs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateTransactionDto {\n");
    
    sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
    sb.append("    outputs: ").append(toIndentedString(outputs)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

