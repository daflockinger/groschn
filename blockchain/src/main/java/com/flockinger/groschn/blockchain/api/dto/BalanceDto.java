package com.flockinger.groschn.blockchain.api.dto;

import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * BalanceDto
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class BalanceDto   {
  @JsonProperty("balance")
  private String balance = null;

  /**
   * Current available Balance (in Groschn).
   * @return balance
  **/
  @ApiModelProperty(value = "Current available Balance (in Groschn).")


  public String getBalance() {
    return balance;
  }

  public void setBalance(String balance) {
    this.balance = balance;
  }
}

