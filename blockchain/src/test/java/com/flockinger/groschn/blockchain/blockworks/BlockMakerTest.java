package com.flockinger.groschn.blockchain.blockworks;

import static com.flockinger.groschn.blockchain.TestDataFactory.createRandomTransactionInputWith;
import static com.flockinger.groschn.blockchain.TestDataFactory.createRandomTransactionOutputWith;
import static com.flockinger.groschn.blockchain.TestDataFactory.createRandomTransactionWith;
import static com.flockinger.groschn.blockchain.TestDataFactory.mapToTransaction;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockMakerImpl;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.exception.ReachingConsentFailedException;
import com.flockinger.groschn.blockchain.exception.validation.BlockValidationException;
import com.flockinger.groschn.blockchain.messaging.MessagingUtils;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.serialize.impl.FstSerializer;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.messaging.model.MessagePayload;
import com.flockinger.groschn.messaging.outbound.Broadcaster;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BlockMakerImpl.class, MessagingUtils.class, CompressionUtils.class, FstSerializer.class})
@Import(TestConfig.class)
public class BlockMakerTest extends BaseCachingTest {

  @MockBean
  private ConsensusFactory consensusFactory;
  @MockBean
  private TransactionManager transactionManager;
  @MockBean
  private BlockStorageService storageService;
  @MockBean
  private Broadcaster<MessagePayload> broadcaster;

  @MockBean
  private Bookkeeper bookkeeper;
  @MockBean
  private WalletService wallet;

  @Autowired
  private ModelMapper mapper;

  @Autowired
  private BlockMaker maker;

  private final String MASTER_KEY = "master-key";
  private final String MASTER_SIGNATURE = "x0x0x0";
  private final String MASTER_HASH = UUID.randomUUID().toString();

  @Before
  public void setup() {
    when(storageService.getLatestBlock()).thenReturn(Block.GENESIS_BLOCK());
    when(bookkeeper.countChange(any())).thenReturn(new BigDecimal("1"));
    when(bookkeeper.calculateBlockReward(anyLong())).thenReturn(new BigDecimal("100"));
  }

  @Test
  public void testProduceBlock_withValidTransactionsWithoutOnesFromTheMiner_shouldCreateStoreAndRewardBlock() {
    var transactions = createRandomTransactions(Optional.empty(), Optional.empty(), true);
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(transactions);
    mockValid();

    maker.produceBlock();

    verifyRegularMocks();
    ArgumentCaptor<List> transactionsCaptor = ArgumentCaptor.forClass(List.class);
    verify(consensusFactory, times(1)).reachConsensus(transactionsCaptor.capture());
    List<Transaction> finishedTransactions = transactionsCaptor.getValue();

    assertNotNull("verify transactions is not null", finishedTransactions);
    assertEquals("verify that it contains the reward transaction", 7, finishedTransactions.size());
    Transaction rewardTransaction = finishedTransactions.get(6);
    assertEquals("verify reward transaction transaction-hash", MASTER_HASH,
        rewardTransaction.getTransactionHash());
    TransactionInput rewardInput = rewardTransaction.getInputs().get(0);
    assertTrue("verify reward transaction input amount",
        new BigDecimal("100").compareTo(rewardInput.getAmount()) == 0);
    assertEquals("verify reward transaction input key", MASTER_KEY, rewardInput.getPublicKey());
    assertEquals("verify reward transaction input sequence nr.", 1l,
        rewardInput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction input has timestamp", rewardInput.getTimestamp());
    assertEquals("verify reward transaction input signature", MASTER_SIGNATURE,
        rewardInput.getSignature());
    assertFalse("verify reward transaction has outputs", rewardTransaction.getOutputs().isEmpty());
    TransactionOutput rewardOutput = rewardTransaction.getOutputs().get(0);
    assertTrue("verify reward transaction output amount",
        new BigDecimal("100").compareTo(rewardOutput.getAmount()) == 0);
    assertEquals("verify reward transaction output key", MASTER_KEY, rewardOutput.getPublicKey());
    assertEquals("verify reward transaction output sequence nr.", 1l,
        rewardOutput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction output has timestamp", rewardOutput.getTimestamp());
    TransactionOutput changeOutput = rewardTransaction.getOutputs().get(1);
    assertTrue("verify change transaction output amount",
        new BigDecimal("6").compareTo(changeOutput.getAmount()) == 0);
    assertEquals("verify change transaction output key", MASTER_KEY, changeOutput.getPublicKey());
    assertEquals("verify change transaction output sequence nr.", 2l,
        changeOutput.getSequenceNumber().longValue());
    assertNotNull("verify change transaction output has timestamp", changeOutput.getTimestamp());
  }

  @Test
  public void testProduceBlock_withValidTransactionsWithOnlyAnIncomeOneFromTheMiner_shouldCreateStoreAndRewardBlock() {
    var transactions = createRandomTransactions(Optional.of(MASTER_KEY), Optional.of(10l), true);
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(transactions);
    mockValid();

    maker.produceBlock();

    verifyRegularMocks();
    ArgumentCaptor<List> transactionsCaptor = ArgumentCaptor.forClass(List.class);
    verify(consensusFactory, times(1)).reachConsensus(transactionsCaptor.capture());
    List<Transaction> finishedTransactions = transactionsCaptor.getValue();

    assertNotNull("verify transactions is not null", finishedTransactions);
    assertEquals("verify that it contains the reward transaction", 8, finishedTransactions.size());
    Transaction rewardTransaction = finishedTransactions.get(7);
    assertEquals("verify reward transaction transaction-hash", MASTER_HASH,
        rewardTransaction.getTransactionHash());
    TransactionInput rewardInput = rewardTransaction.getInputs().get(0);
    assertTrue("verify reward transaction input amount",
        new BigDecimal("100").compareTo(rewardInput.getAmount()) == 0);
    assertEquals("verify reward transaction input key", MASTER_KEY, rewardInput.getPublicKey());
    assertEquals("verify reward transaction input sequence nr.", 1l,
        rewardInput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction input has timestamp", rewardInput.getTimestamp());
    assertEquals("verify reward transaction input signature", MASTER_SIGNATURE,
        rewardInput.getSignature());
    assertFalse("verify reward transaction has outputs", rewardTransaction.getOutputs().isEmpty());
    TransactionOutput rewardOutput = rewardTransaction.getOutputs().get(0);
    assertTrue("verify reward transaction output amount",
        new BigDecimal("100").compareTo(rewardOutput.getAmount()) == 0);
    assertEquals("verify reward transaction output key", MASTER_KEY, rewardOutput.getPublicKey());
    assertEquals("verify reward transaction output sequence nr.", 1l,
        rewardOutput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction output has timestamp", rewardOutput.getTimestamp());
    TransactionOutput changeOutput = rewardTransaction.getOutputs().get(1);
    assertTrue("verify change transaction output amount",
        new BigDecimal("7").compareTo(changeOutput.getAmount()) == 0);
    assertEquals("verify change transaction output key", MASTER_KEY, changeOutput.getPublicKey());
    assertEquals("verify change transaction output sequence nr.", 2l,
        changeOutput.getSequenceNumber().longValue());
    assertNotNull("verify change transaction output has timestamp", changeOutput.getTimestamp());
  }


  /**
   * If in the list of transactions there's also one from the pubKey, then the reward must be added
   * to this one, since one block can contain only ONE expense transaction (where the pubKey owner
   * spends currency) per publicKey (to ensure correct and easy balance calculation).
   */
  @Test
  public void testProduceBlock_withValidTransactionsWithAlsoAnExpenseOneFromTheMiner_shouldCreateStoreAndAddRewardBlock() {
    var transactions = createRandomTransactions(Optional.of(MASTER_KEY), Optional.of(10l), false);
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(transactions);
    mockValid();

    maker.produceBlock();

    verifyRegularMocks();
    ArgumentCaptor<List> transactionsCaptor = ArgumentCaptor.forClass(List.class);
    verify(consensusFactory, times(1)).reachConsensus(transactionsCaptor.capture());
    List<Transaction> finishedTransactions = transactionsCaptor.getValue();

    assertNotNull("verify transactions is not null", finishedTransactions);
    assertEquals("verify that it contains the reward transaction", 7, finishedTransactions.size());
    Transaction rewardTransaction = finishedTransactions.get(6);
    assertEquals("verify reward transaction transaction-hash", MASTER_HASH,
        rewardTransaction.getTransactionHash());
    TransactionInput rewardInput = rewardTransaction.getInputs().get(4);
    assertTrue("verify reward transaction input amount",
        new BigDecimal("100").compareTo(rewardInput.getAmount()) == 0);
    assertEquals("verify reward transaction input key", MASTER_KEY, rewardInput.getPublicKey());
    assertEquals("verify reward transaction input sequence nr.", 5l,
        rewardInput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction input has timestamp", rewardInput.getTimestamp());
    assertEquals("verify reward transaction input signature", MASTER_SIGNATURE,
        rewardInput.getSignature());
    assertFalse("verify reward transaction has outputs", rewardTransaction.getOutputs().isEmpty());
    TransactionOutput rewardOutput = rewardTransaction.getOutputs().get(4);
    assertTrue("verify reward transaction output amount",
        new BigDecimal("100").compareTo(rewardOutput.getAmount()) == 0);
    assertEquals("verify reward transaction output key", MASTER_KEY, rewardOutput.getPublicKey());
    assertEquals("verify reward transaction output sequence nr.", 5l,
        rewardOutput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction output has timestamp", rewardOutput.getTimestamp());
    TransactionOutput changeOutput = rewardTransaction.getOutputs().get(5);
    assertTrue("verify change transaction output amount",
        new BigDecimal("7").compareTo(changeOutput.getAmount()) == 0);
    assertEquals("verify change transaction output key", MASTER_KEY, changeOutput.getPublicKey());
    assertEquals("verify change transaction output sequence nr.", 6l,
        changeOutput.getSequenceNumber().longValue());
    assertNotNull("verify change transaction output has timestamp", changeOutput.getTimestamp());

    assertTrue("verify that reward transaction contains an input record of the same publicKey",
        rewardTransaction.getInputs().stream()
            .anyMatch(input -> input.getPublicKey().equals(MASTER_KEY)));
    finishedTransactions.remove(rewardTransaction);
    assertFalse(
        "verify that all other transactions DON'T contain an input record of the same publicKey",
        finishedTransactions.stream().anyMatch(transaction -> transaction.getInputs().stream()
            .anyMatch(input -> input.getPublicKey().equals(MASTER_KEY))));
  }

  @Test
  public void testProduceBlock_withEmptyTransactions_shouldCreateRewardOnlyBlock() {
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    mockValid();

    maker.produceBlock();

    verifyRegularMocks();
    ArgumentCaptor<List> transactionsCaptor = ArgumentCaptor.forClass(List.class);
    verify(consensusFactory, times(1)).reachConsensus(transactionsCaptor.capture());
    List<Transaction> transactions = transactionsCaptor.getValue();

    assertNotNull("verify transactions is not null", transactions);
    assertEquals("verify that it contains the reward transaction", 1, transactions.size());
    assertFalse("verify reward transaction has inputs", transactions.get(0).getInputs().isEmpty());
    TransactionInput input = transactions.get(0).getInputs().get(0);
    assertTrue("verify reward transaction input amount",
        new BigDecimal("100").compareTo(input.getAmount()) == 0);
    assertEquals("verify reward transaction input key", MASTER_KEY, input.getPublicKey());
    assertEquals("verify reward transaction input sequence nr.", 1l,
        input.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction input has timestamp", input.getTimestamp());
    assertFalse("verify reward transaction has outputs",
        transactions.get(0).getOutputs().isEmpty());
    TransactionOutput output = transactions.get(0).getOutputs().get(0);
    assertTrue("verify reward transaction output amount",
        new BigDecimal("100").compareTo(output.getAmount()) == 0);
    assertEquals("verify reward transaction output key", MASTER_KEY, output.getPublicKey());
    assertEquals("verify reward transaction output sequence nr.", 1l,
        output.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction output has timestamp", output.getTimestamp());
  }



  @Test
  public void testProduceBlock_withStorageServiceThrowingException_shouldDoNothing() {
    when(consensusFactory.reachConsensus(anyList())).thenReturn(new Block());
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());
    when(storageService.saveInBlockchain(any())).thenThrow(BlockValidationException.class);

    maker.produceBlock();
  }

  @Test
  public void testProduceBlock_withConsensusFactoryThrowingException_shouldDoNothing() {
    when(consensusFactory.reachConsensus(anyList()))
        .thenThrow(ReachingConsentFailedException.class);
    when(transactionManager.fetchTransactionsBySize(anyLong())).thenReturn(new ArrayList<>());

    maker.produceBlock();
  }


  private void verifyRegularMocks() {
    verify(transactionManager).fetchTransactionsBySize(anyLong());
    ArgumentCaptor<TransactionDto> requestCaptor = ArgumentCaptor.forClass(TransactionDto.class);
    verify(transactionManager).createSignedTransaction(requestCaptor.capture());
    TransactionDto request = requestCaptor.getValue();
    assertEquals("verify reward transaction request contains a valid public key", MASTER_KEY,
        request.getPublicKey());
    verify(broadcaster, times(1)).broadcast(any(),any());
    verify(storageService).saveInBlockchain(any());
    verify(storageService).saveInBlockchain(any());
  }

  private void mockValid() {
    when(consensusFactory.reachConsensus(anyList())).thenReturn(new Block());
    when(transactionManager.createSignedTransaction(any())).thenAnswer(new Answer<Transaction>() {
      public Transaction answer(InvocationOnMock invocation) throws Throwable {
        Transaction signedTransaction = mapper.map(invocation.getArguments()[0], Transaction.class);
        signedTransaction.getInputs().stream()
            .filter(input -> input.getPublicKey().equals(MASTER_KEY))
            .forEach(input -> input.setSignature(MASTER_SIGNATURE));
        signedTransaction.setTransactionHash(MASTER_HASH);
        return signedTransaction;
      }
    });
    when(wallet.getNodePublicKey()).thenReturn(MASTER_KEY);
  }

  public List<Transaction> createRandomTransactions(Optional<String> minerKey,
      Optional<Long> amount, boolean noInput) {
    var transactions = new ArrayList<Transaction>();
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    if (minerKey.isPresent() && amount.isPresent()) {
      transactions.add(mapToTransaction(createRandomTransactionWith(null,
          createRandomTransactionOutputWith(2, minerKey.get(), amount.get()),
          (noInput) ? createRandomTransactionInputWith(2)
              : createRandomTransactionInputWith(2, minerKey.get(), amount.get()))));
    }
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    return transactions;
  }
}
