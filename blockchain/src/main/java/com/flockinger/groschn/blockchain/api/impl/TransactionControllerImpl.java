package com.flockinger.groschn.blockchain.api.impl;

import java.util.List;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.flockinger.groschn.blockchain.api.TransactionController;
import com.flockinger.groschn.blockchain.api.dto.CreateTransactionDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionIdDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionStatusDto;
import com.flockinger.groschn.blockchain.api.dto.ViewTransactionDto;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.messaging.config.MainTopics;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import io.swagger.annotations.ApiParam;

@RestController
public class TransactionControllerImpl implements TransactionController {
  
  @Autowired
  private TransactionManager manager;
  @Autowired
  private Broadcaster<MessagePayload> broadcaster;
  @Autowired
  private ModelMapper mapper;
  
  @Value("${atomix.node-id}")
  private String nodeId;

  public ResponseEntity<TransactionStatusDto> getTransactionStatus(
      @ApiParam(value = "Unique identifier of the transaction.",
          required = true) @PathVariable("transaction-id") String transactionId) {

    var status = manager.getStatusOfTransaction(transactionId);
    return new ResponseEntity<TransactionStatusDto>(status, HttpStatus.OK);
  }

  public ResponseEntity<List<ViewTransactionDto>> getTransactionsFromPublicKey(
      @ApiParam(value = "Public key.", required = true) @PathVariable("pub-key") String pubKey) {

    var views = manager.getTransactionsFromPublicKey(pubKey);
    return new ResponseEntity<List<ViewTransactionDto>>(views, HttpStatus.OK);
  }

  public ResponseEntity<TransactionIdDto> publishTransaction(@ApiParam(value = "Transaction data",
      required = true) @Valid @RequestBody CreateTransactionDto createTransaction) {
    
    var transaction = mapper.map(createTransaction, Transaction.class);
    var id = manager.storeTransaction(transaction);
    broadcaster.broadcast(transaction, nodeId, MainTopics.FRESH_TRANSACTION);
    return new ResponseEntity<TransactionIdDto>(id, HttpStatus.CREATED);
  }
}
