package com.flockinger.groschn.blockchain.api.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * TransactionInputDTO
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class TransactionInputDto   {
  @JsonProperty("signature")
  @NotEmpty
  private String signature = null;

  @JsonProperty("amount")
  @NotEmpty
  private String amount = null;

  @JsonProperty("publicKey")
  @NotEmpty
  private String publicKey = null;

  @JsonProperty("timestamp")
  @NotNull
  private Long timestamp = null;

  @JsonProperty("sequenceNumber")
  @NotNull
  private Long sequenceNumber = null;


  /**
   * Signature from the transaction sender.
   * @return signature
  **/
  @ApiModelProperty(value = "Signature from the transaction sender.")
  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }


  /**
   * Amount (Groschn) to transfer.
   * @return amount
  **/
  @ApiModelProperty(value = "Amount (Groschn) to transfer.")
  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  /**
   * Public key of the transaction sender.
   * @return publicKey
  **/
  @ApiModelProperty(value = "Public key of the transaction sender.")
  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }


  /**
   * Input timestamp (milliseconds).
   * @return timestamp
  **/
  @ApiModelProperty(value = "Input timestamp (milliseconds).")
  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }


  /**
   * Ordered sequence number of the input (starts with 1).
   * @return sequenceNumber
  **/
  @ApiModelProperty(value = "Ordered sequence number of the input (starts with 1).")
  public Long getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }
}

