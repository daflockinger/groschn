package com.flockinger.groschn.blockchain.api.impl;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.flockinger.groschn.blockchain.api.TransactionController;
import com.flockinger.groschn.blockchain.api.dto.CreateTransactionDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionIdDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionStatus;
import com.flockinger.groschn.blockchain.api.dto.ViewTransactionDto;
import io.swagger.annotations.ApiParam;

@RestController
public class TransactionControllerImpl implements TransactionController {


  public ResponseEntity<TransactionStatus> getTransactionStatus(
      @ApiParam(value = "Unique identifier of the transaction.",
          required = true) @PathVariable("transaction-id") String transactionId) {


    return new ResponseEntity<TransactionStatus>(HttpStatus.NOT_IMPLEMENTED);
  }

  public ResponseEntity<List<ViewTransactionDto>> getTransactionsFromPublicKey(
      @ApiParam(value = "Public key.", required = true) @PathVariable("pub-key") String pubKey) {


    return new ResponseEntity<List<ViewTransactionDto>>(HttpStatus.NOT_IMPLEMENTED);
  }

  public ResponseEntity<TransactionIdDto> publishTransaction(@ApiParam(value = "Transaction data",
      required = true) @Valid @RequestBody CreateTransactionDto createTransaction) {

    return new ResponseEntity<TransactionIdDto>(HttpStatus.NOT_IMPLEMENTED);
  }
}
