package com.flockinger.groschn.blockchain.api.dto;

import java.util.Objects;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * TransactionInputDTO
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class TransactionInputDTO   {
  @JsonProperty("signature")
  private String signature = null;

  @JsonProperty("amount")
  private String amount = null;

  @JsonProperty("publicKey")
  private String publicKey = null;

  @JsonProperty("timestamp")
  private Long timestamp = null;

  @JsonProperty("sequenceNumber")
  private Long sequenceNumber = null;

  public TransactionInputDTO signature(String signature) {
    this.signature = signature;
    return this;
  }

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

  public TransactionInputDTO amount(String amount) {
    this.amount = amount;
    return this;
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

  public TransactionInputDTO publicKey(String publicKey) {
    this.publicKey = publicKey;
    return this;
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

  public TransactionInputDTO timestamp(Long timestamp) {
    this.timestamp = timestamp;
    return this;
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

  public TransactionInputDTO sequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
    return this;
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionInputDTO transactionInputDTO = (TransactionInputDTO) o;
    return Objects.equals(this.signature, transactionInputDTO.signature) &&
        Objects.equals(this.amount, transactionInputDTO.amount) &&
        Objects.equals(this.publicKey, transactionInputDTO.publicKey) &&
        Objects.equals(this.timestamp, transactionInputDTO.timestamp) &&
        Objects.equals(this.sequenceNumber, transactionInputDTO.sequenceNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(signature, amount, publicKey, timestamp, sequenceNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionInputDTO {\n");
    
    sb.append("    signature: ").append(toIndentedString(signature)).append("\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    publicKey: ").append(toIndentedString(publicKey)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    sequenceNumber: ").append(toIndentedString(sequenceNumber)).append("\n");
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

