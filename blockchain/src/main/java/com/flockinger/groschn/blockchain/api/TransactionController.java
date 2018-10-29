package com.flockinger.groschn.blockchain.api;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.flockinger.groschn.blockchain.api.dto.CreateTransactionDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionIdDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionStatusDto;
import com.flockinger.groschn.blockchain.api.dto.ViewTransactionDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public interface TransactionController {


  @ApiOperation(value = "Get status of the Transaction.", nickname = "getTransactionStatus",
      notes = "Fetches status information of the Transaction.", response = TransactionStatusDto.class,
      tags = {"Transaction",})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "TransactionStatus.", response = TransactionStatusDto.class),
      @ApiResponse(code = 400, message = "Bad request (validation failed).",
          response = Error.class),
      @ApiResponse(code = 401, message = "Unauthorized (need to log in / get token)."),
      @ApiResponse(code = 403, message = "Forbidden (no rights to access resource)."),
      @ApiResponse(code = 404, message = "Entity not found.", response = Error.class),
      @ApiResponse(code = 409, message = "Request results in a conflict.", response = Error.class),
      @ApiResponse(code = 500, message = "Internal Server Error.")})
  @RequestMapping(value = "/api/v1/groschn/transaction/status/{transaction-id}",
      produces = {"application/json"}, method = RequestMethod.GET)
  ResponseEntity<TransactionStatusDto> getTransactionStatus(
      @ApiParam(value = "Unique identifier of the transaction.",
          required = true) @PathVariable("transaction-id") String transactionId);


  @ApiOperation(value = "Get all transactions from public key.",
      nickname = "getTransactionsFromPublicKey",
      notes = "Fetches all transactions from a specific public key.",
      response = ViewTransactionDto.class, responseContainer = "List", tags = {"Transaction",})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Transactions.", response = ViewTransactionDto.class,
          responseContainer = "List"),
      @ApiResponse(code = 400, message = "Bad request (validation failed).",
          response = Error.class),
      @ApiResponse(code = 401, message = "Unauthorized (need to log in / get token)."),
      @ApiResponse(code = 403, message = "Forbidden (no rights to access resource)."),
      @ApiResponse(code = 404, message = "Entity not found.", response = Error.class),
      @ApiResponse(code = 409, message = "Request results in a conflict.", response = Error.class),
      @ApiResponse(code = 500, message = "Internal Server Error.")})
  @RequestMapping(value = "/api/v1/groschn/transaction/{pub-key}", produces = {"application/json"},
      method = RequestMethod.GET)
  ResponseEntity<List<ViewTransactionDto>> getTransactionsFromPublicKey(
      @ApiParam(value = "Public key.", required = true) @PathVariable("pub-key") String pubKey);


  @ApiOperation(value = "Create and publish Transaction.", nickname = "publishTransaction",
      notes = "Creates and verifies a Transaction and then publishes it if it's fine.",
      response = TransactionIdDto.class, tags = {"Transaction",})
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Transaction created and published.",
          response = TransactionIdDto.class),
      @ApiResponse(code = 400, message = "Bad request (validation failed).",
          response = Error.class),
      @ApiResponse(code = 401, message = "Unauthorized (need to log in / get token)."),
      @ApiResponse(code = 403, message = "Forbidden (no rights to access resource)."),
      @ApiResponse(code = 404, message = "Entity not found.", response = Error.class),
      @ApiResponse(code = 409, message = "Request results in a conflict.", response = Error.class),
      @ApiResponse(code = 500, message = "Internal Server Error.")})
  @RequestMapping(value = "/api/v1/groschn/transaction", produces = {"application/json"},
      consumes = {"application/json"}, method = RequestMethod.POST)
  ResponseEntity<TransactionIdDto> publishTransaction(@ApiParam(value = "Transaction data",
      required = true) @Valid @RequestBody CreateTransactionDto createTransaction);
}
