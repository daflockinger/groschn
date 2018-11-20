package com.flockinger.groschn.blockchain.api.dto;

import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * TransactionIdDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class TransactionIdDto   {
  @JsonProperty("id")
  private String id = null;

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
  
  public TransactionIdDto id(String id) {
    this.id = id;
    return this;
  }
}

