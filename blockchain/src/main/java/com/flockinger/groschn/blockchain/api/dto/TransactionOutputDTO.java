package com.flockinger.groschn.blockchain.api.dto;

import java.util.Objects;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * TransactionOutputDTO
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class TransactionOutputDTO   {
  @JsonProperty("amount")
  private String amount = null;

  @JsonProperty("publicKey")
  private String publicKey = null;

  @JsonProperty("timestamp")
  private Long timestamp = null;

  @JsonProperty("sequenceNumber")
  private Long sequenceNumber = null;

  public TransactionOutputDTO amount(String amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Amount (Groschn) transfered.
   * @return amount
  **/
  @ApiModelProperty(value = "Amount (Groschn) transfered.")


  public String getAmount() {
    return amount;
  }

  public void setAmount(String amount) {
    this.amount = amount;
  }

  public TransactionOutputDTO publicKey(String publicKey) {
    this.publicKey = publicKey;
    return this;
  }

  /**
   * Public key of the transaction receiver.
   * @return publicKey
  **/
  @ApiModelProperty(value = "Public key of the transaction receiver.")


  public String getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public TransactionOutputDTO timestamp(Long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Output timestamp (milliseconds).
   * @return timestamp
  **/
  @ApiModelProperty(value = "Output timestamp (milliseconds).")


  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public TransactionOutputDTO sequenceNumber(Long sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
    return this;
  }

  /**
   * Ordered sequence number of the output (starts with 1).
   * @return sequenceNumber
  **/
  @ApiModelProperty(value = "Ordered sequence number of the output (starts with 1).")


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
    TransactionOutputDTO transactionOutputDTO = (TransactionOutputDTO) o;
    return Objects.equals(this.amount, transactionOutputDTO.amount) &&
        Objects.equals(this.publicKey, transactionOutputDTO.publicKey) &&
        Objects.equals(this.timestamp, transactionOutputDTO.timestamp) &&
        Objects.equals(this.sequenceNumber, transactionOutputDTO.sequenceNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, publicKey, timestamp, sequenceNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionOutputDTO {\n");
    
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

