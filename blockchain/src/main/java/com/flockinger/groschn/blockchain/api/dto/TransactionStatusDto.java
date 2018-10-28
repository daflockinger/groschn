package com.flockinger.groschn.blockchain.api.dto;

import java.util.Objects;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * TransactionStatus
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-27T19:27:44.622Z")

public class TransactionStatusDto   {
  @JsonProperty("status")
  private String status = null;

  @JsonProperty("statusMessage")
  private String statusMessage = null;

  public TransactionStatusDto status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Status of the transaction.
   * @return status
  **/
  @ApiModelProperty(value = "Status of the transaction.")


  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public TransactionStatusDto statusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
    return this;
  }

  /**
   * Detailed status message of the transaction.
   * @return statusMessage
  **/
  @ApiModelProperty(value = "Detailed status message of the transaction.")


  public String getStatusMessage() {
    return statusMessage;
  }

  public void setStatusMessage(String statusMessage) {
    this.statusMessage = statusMessage;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionStatusDto transactionStatus = (TransactionStatusDto) o;
    return Objects.equals(this.status, transactionStatus.status) &&
        Objects.equals(this.statusMessage, transactionStatus.statusMessage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, statusMessage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionStatus {\n");
    
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    statusMessage: ").append(toIndentedString(statusMessage)).append("\n");
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

