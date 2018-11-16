package com.flockinger.groschn.blockchain.api;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.TestDataFactory;
import com.flockinger.groschn.blockchain.api.dto.CreateTransactionDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionIdDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionStatusDto;
import com.flockinger.groschn.blockchain.api.dto.ViewTransactionDto;
import com.flockinger.groschn.blockchain.api.impl.TransactionControllerImpl;
import com.flockinger.groschn.blockchain.exception.TransactionAlreadyClearedException;
import com.flockinger.groschn.blockchain.exception.TransactionNotFoundException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.commons.compress.CompressionUtils;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.flockinger.groschn.messaging.util.MessagingUtils;
import com.google.common.collect.ImmutableList;

@WebMvcTest(controllers = {TransactionControllerImpl.class, MessagingUtils.class})
@Import(TestConfig.class)
@TestPropertySource(properties="atomix.node-id=1234")
public class TransactionControllerTest extends BaseControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ModelMapper mapper;
  
  @MockBean
  private CompressionUtils mockCompressor;
  @MockBean
  private TransactionManager manager;
  @MockBean
  private Broadcaster<MessagePayload> broadcaster;
  

  @Test
  public void testGetTransactionStatus_withValidTxId_shouldReturnCorrect() throws Exception {
    when(manager.getStatusOfTransaction(any())).thenReturn(new TransactionStatusDto());

    mockMvc.perform(get("/api/v1/groschn/transaction/status/1234").contentType(jsonContentType))
        .andExpect(status().isOk()).andExpect(jsonPath("$", notNullValue()));

    verify(manager).getStatusOfTransaction(matches("1234"));
  }

  @Test
  public void testGetTransactionStatus_withInalidTxId_shouldReturnNotFound() throws Exception {
    when(manager.getStatusOfTransaction(any())).thenThrow(TransactionNotFoundException.class);

    mockMvc
        .perform(
            get("/api/v1/groschn/transaction/status/nonExistante").contentType(jsonContentType))
        .andExpect(status().isNotFound());
  }


  @Test
  public void testGetTransactionsFromPublicKey_withValidPublicKey_shouldReturnCorrect()
      throws Exception {
    when(manager.getTransactionsFromPublicKey(anyString()))
    .thenReturn(ImmutableList.of(new ViewTransactionDto(), new ViewTransactionDto()));

    mockMvc.perform(get("/api/v1/groschn/transaction/1234").contentType(jsonContentType))
        .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

    verify(manager).getTransactionsFromPublicKey(matches("1234"));
  }

  @Test
  public void testGetTransactionsFromPublicKey_withInalidPublicKey_shouldReturnEmpty()
      throws Exception {
    when(manager.getTransactionsFromPublicKey(anyString()))
    .thenReturn(new ArrayList<>());

    mockMvc.perform(get("/api/v1/groschn/transaction/nonExistante").contentType(jsonContentType))
        .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));

    verify(manager).getTransactionsFromPublicKey(matches("nonExistante"));
  }
  
  @Test
  public void testPublishTransaction_withValidTransaction_shouldStoreAndReturnId() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    when(manager.storeTransaction(any())).thenReturn(new TransactionIdDto().id("1234"));
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.id", is("1234")));
    
    verify(manager).storeTransaction(any());
    verify(broadcaster).broadcast(any(), any(), any());
  }
  
  @Test
  public void testPublishTransaction_withInvalidTransaction_shouldReturnConflict() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    when(manager.storeTransaction(any())).thenThrow(AssessmentFailedException.class);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isConflict());
  }
  
  @Test
  public void testPublishTransaction_withAlreadyExistingTransaction_shouldReturnConflict() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    when(manager.storeTransaction(any())).thenThrow(TransactionAlreadyClearedException.class);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isConflict());
  }
  
  @Test
  public void testPublishTransaction_withTxInputsNull_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.setInputs(null);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoInputAmount_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getInputs().get(0).setAmount("");
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoOutputAmount_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getOutputs().get(0).setAmount("");
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoOutputPubKey_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getOutputs().get(0).publicKey("");
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoInputPubKey_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getInputs().get(0).publicKey("");
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoInputSequenceNumber_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getInputs().get(0).setSequenceNumber(null);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoOutputSequenceNumber_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getOutputs().get(0).setSequenceNumber(null);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoOutputTimestamp_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getOutputs().get(0).setTimestamp(null);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoInputTimestamp_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getOutputs().get(0).setTimestamp(null);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withNoInputSignature_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.getInputs().get(0).setSignature("");
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  @Test
  public void testPublishTransaction_withTxOutputsNull_shouldReturnBadRequest() throws IOException, Exception {
    CreateTransactionDto transaction = mapper.map(TestDataFactory
        .createRandomTransactionWiths(null, null), CreateTransactionDto.class);
    transaction.setOutputs(null);
    
    mockMvc
    .perform(
        post("/api/v1/groschn/transaction").contentType(jsonContentType).content(json(transaction)))
    .andExpect(status().isBadRequest());
  }
  
  /*
   * @ApiOperation(value = "Get status of the Transaction.", nickname = "getTransactionStatus",
   * notes = "Fetches status information of the Transaction.", response =
   * TransactionStatusDto.class, tags = {"Transaction",})
   * 
   * @RequestMapping(value = "/api/v1/groschn/transaction/status/{transaction-id}", produces =
   * {"application/json"}, method = RequestMethod.GET) ResponseEntity<TransactionStatusDto>
   * getTransactionStatus(
   * 
   * @ApiParam(value = "Unique identifier of the transaction.", required =
   * true) @PathVariable("transaction-id") String transactionId);
   * 
   * 
   * @ApiOperation(value = "Get all transactions from public key.", nickname =
   * "getTransactionsFromPublicKey", notes = "Fetches all transactions from a specific public key.",
   * response = ViewTransactionDto.class, responseContainer = "List", tags = {"Transaction",})
   * 
   * @RequestMapping(value = "/api/v1/groschn/transaction/{pub-key}", produces =
   * {"application/json"}, method = RequestMethod.GET) ResponseEntity<List<ViewTransactionDto>>
   * getTransactionsFromPublicKey(
   * 
   * @ApiParam(value = "Public key.", required = true) @PathVariable("pub-key") String pubKey);
   * 
   * 
   * @ApiOperation(value = "Create and publish Transaction.", nickname = "publishTransaction", notes
   * = "Creates and verifies a Transaction and then publishes it if it's fine.", response =
   * TransactionIdDto.class, tags = {"Transaction",})
   * 
   * @RequestMapping(value = "/api/v1/groschn/transaction", produces = {"application/json"},
   * consumes = {"application/json"}, method = RequestMethod.POST) ResponseEntity<TransactionIdDto>
   * publishTransaction(@ApiParam(value = "Transaction data", required = true) @Valid @RequestBody
   * CreateTransactionDto createTransaction);
   */
}
