package com.flockinger.groschn.blockchain.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * ViewTransactionDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class ViewTransactionDto   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("inputs")
  @Valid
  private List<TransactionInputDto> inputs = null;

  @JsonProperty("outputs")
  @Valid
  private List<TransactionOutputDto> outputs = null;

  public ViewTransactionDto id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Unique identifier of the created transaction.
   * @return id
  **/
  @ApiModelProperty(value = "Unique identifier of the created transaction.")


  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ViewTransactionDto inputs(List<TransactionInputDto> inputs) {
    this.inputs = inputs;
    return this;
  }

  public ViewTransactionDto addInputsItem(TransactionInputDto inputsItem) {
    if (this.inputs == null) {
      this.inputs = new ArrayList<TransactionInputDto>();
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

  public List<TransactionInputDto> getInputs() {
    return inputs;
  }

  public void setInputs(List<TransactionInputDto> inputs) {
    this.inputs = inputs;
  }

  public ViewTransactionDto outputs(List<TransactionOutputDto> outputs) {
    this.outputs = outputs;
    return this;
  }

  public ViewTransactionDto addOutputsItem(TransactionOutputDto outputsItem) {
    if (this.outputs == null) {
      this.outputs = new ArrayList<TransactionOutputDto>();
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

  public List<TransactionOutputDto> getOutputs() {
    return outputs;
  }

  public void setOutputs(List<TransactionOutputDto> outputs) {
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
    ViewTransactionDto viewTransactionDto = (ViewTransactionDto) o;
    return Objects.equals(this.id, viewTransactionDto.id) &&
        Objects.equals(this.inputs, viewTransactionDto.inputs) &&
        Objects.equals(this.outputs, viewTransactionDto.outputs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, inputs, outputs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ViewTransactionDto {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

