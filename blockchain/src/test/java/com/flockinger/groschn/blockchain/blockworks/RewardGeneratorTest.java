package com.flockinger.groschn.blockchain.blockworks;

import static com.flockinger.groschn.blockchain.TestDataFactory.createRandomTransactions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.BaseCachingTest;
import com.flockinger.groschn.blockchain.TestConfig;
import com.flockinger.groschn.blockchain.blockworks.impl.RewardGeneratorImpl;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import java.math.BigDecimal;
import java.util.ArrayList;
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

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {RewardGeneratorImpl.class})
@Import(TestConfig.class)
public class RewardGeneratorTest extends BaseCachingTest {
  
  @MockBean
  private TransactionManager transactionManager;
  @MockBean
  private Bookkeeper bookkeeper;
  @MockBean
  private WalletService wallet;

  @Autowired
  private ModelMapper mapper;
  @Autowired
  private RewardGenerator generator;
  
  private final String MASTER_KEY = "master-key";
  private final String MASTER_SIGNATURE = "x0x0x0";
  private final String MASTER_HASH = UUID.randomUUID().toString();

  @Before
  public void setup() {
    when(bookkeeper.countChange(any())).thenReturn(new BigDecimal("1"));
    when(bookkeeper.calculateCurrentBlockReward()).thenReturn(new BigDecimal("100"));
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
  
  @Test
  public void testGenerateRewardTransaction_withValidTransactionsWithoutOnesFromTheMiner_shouldReturnFreshReward() {
    var transactions = createRandomTransactions(Optional.empty(), Optional.empty(), true);
    when(wallet.calculateBalance(anyString())).thenReturn(new BigDecimal(122));

    var transactionsWithReward = generator.generateRewardTransaction(transactions);

    ArgumentCaptor<TransactionDto> requestCaptor = ArgumentCaptor.forClass(TransactionDto.class);
    verify(transactionManager).createSignedTransaction(requestCaptor.capture());
    TransactionDto request = requestCaptor.getValue();
    assertEquals("verify reward transaction request contains a valid public key", MASTER_KEY,
        request.getPublicKey());

    assertNotNull("verify transactions is not null", transactionsWithReward);
    assertEquals("verify that it contains the reward transaction", 7, transactionsWithReward.size());
    Transaction rewardTransaction = transactionsWithReward.get(6);
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
    TransactionInput balanceInput = rewardTransaction.getInputs().get(1);
    assertTrue("verify balance transaction input amount",
        new BigDecimal("122").compareTo(balanceInput.getAmount()) == 0);
    assertEquals("verify balance transaction input key", MASTER_KEY, balanceInput.getPublicKey());
    assertEquals("verify balance transaction input sequence nr.", 2l,
        balanceInput.getSequenceNumber().longValue());
    assertNotNull("verify balance transaction input has timestamp", balanceInput.getTimestamp());
    assertEquals("verify balance transaction input signature", MASTER_SIGNATURE,
        balanceInput.getSignature());
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
    TransactionOutput balanceOutput = rewardTransaction.getOutputs().get(2);
    assertTrue("verify balance transaction output amount",
        new BigDecimal("122").compareTo(balanceOutput.getAmount()) == 0);
    assertEquals("verify balance transaction output key", MASTER_KEY, balanceOutput.getPublicKey());
    assertEquals("verify balance transaction output sequence nr.", 3l,
        balanceOutput.getSequenceNumber().longValue());
    assertNotNull("verify balance transaction output has timestamp", balanceOutput.getTimestamp());
  }
  
  @Test
  public void testGenerateRewardTransaction_withValidTransactionsWithOnlyAnIncomeOneFromTheMiner_shouldAddRewardAndPubKeysBalance() {
    var transactions = createRandomTransactions(Optional.of(MASTER_KEY), Optional.of(10l), true);
    when(wallet.calculateBalance(anyString())).thenReturn(BigDecimal.valueOf(122));

    var transactionsWithReward = generator.generateRewardTransaction(transactions);

    ArgumentCaptor<TransactionDto> requestCaptor = ArgumentCaptor.forClass(TransactionDto.class);
    verify(transactionManager, times(1)).createSignedTransaction(requestCaptor.capture());
    TransactionDto request = requestCaptor.getValue();
    assertEquals("verify reward transaction request contains a valid public key", MASTER_KEY,
        request.getPublicKey());
    verify(wallet).calculateBalance(matches(MASTER_KEY));
    
    assertNotNull("verify transactions is not null", transactionsWithReward);
    assertEquals("verify that it contains the reward transaction", 8, transactionsWithReward.size());
    Transaction rewardTransaction = transactionsWithReward.get(7);
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
    TransactionInput balanceInput = rewardTransaction.getInputs().get(1);
    assertTrue("verify balance transaction input amount",
        new BigDecimal("122").compareTo(balanceInput.getAmount()) == 0);
    assertEquals("verify balance transaction input key", MASTER_KEY, balanceInput.getPublicKey());
    assertEquals("verify balance transaction input sequence nr.", 2l,
        balanceInput.getSequenceNumber().longValue());
    assertNotNull("verify balance transaction input has timestamp", balanceInput.getTimestamp());
    assertEquals("verify balance transaction input signature", MASTER_SIGNATURE,
        balanceInput.getSignature());
    
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
    TransactionOutput balanceOutput = rewardTransaction.getOutputs().get(2);
    assertTrue("verify balance transaction output amount",
        new BigDecimal("122").compareTo(balanceOutput.getAmount()) == 0);
    assertEquals("verify balance transaction output key", MASTER_KEY, balanceOutput.getPublicKey());
    assertEquals("verify balance transaction output sequence nr.", 3l,
        balanceOutput.getSequenceNumber().longValue());
    assertNotNull("verify balance transaction output has timestamp", balanceOutput.getTimestamp());
  }
  
  /**
   * If in the list of transactions there's also one from the pubKey, then the reward must be added
   * to this one, since one block can contain only ONE expense transaction (where the pubKey owner
   * spends currency) per publicKey (to ensure correct and easy balance calculation).
   */
  @Test
  public void testGenerateRewardTransaction_withValidTransactionsWithAlsoAnExpenseOneFromTheMiner_shouldAddRewardTransaction() {
    var transactions = createRandomTransactions(Optional.of(MASTER_KEY), Optional.of(10l), false);

    var transactionsWithReward = generator.generateRewardTransaction(transactions);

    ArgumentCaptor<TransactionDto> requestCaptor = ArgumentCaptor.forClass(TransactionDto.class);
    verify(transactionManager).createSignedTransaction(requestCaptor.capture());
    TransactionDto request = requestCaptor.getValue();
    assertEquals("verify reward transaction request contains a valid public key", MASTER_KEY,
        request.getPublicKey());
    
    assertNotNull("verify transactions is not null", transactionsWithReward);
    assertEquals("verify that it contains the reward transaction", 8, transactionsWithReward.size());
    Transaction rewardTransaction = transactionsWithReward.get(7);
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

    assertTrue("verify that reward transaction contains an input record of the same publicKey",
        rewardTransaction.getInputs().stream()
            .anyMatch(input -> input.getPublicKey().equals(MASTER_KEY)));
    assertEquals("verify that the reward transaction doesn't contain a balance input", 
        1, rewardTransaction.getInputs().size());
    assertEquals("verify that the reward transaction doesn't contain a balance output", 
        2, rewardTransaction.getOutputs().size());
  }
  
  @Test
  public void testGenerateRewardTransaction_withEmptyTransactions_shouldAddRewardAndPubKeysBalance() {
    when(wallet.calculateBalance(anyString())).thenReturn(BigDecimal.valueOf(122));
    
    var transactionsWithReward = generator.generateRewardTransaction(new ArrayList<>());

    ArgumentCaptor<TransactionDto> requestCaptor = ArgumentCaptor.forClass(TransactionDto.class);
    verify(transactionManager,times(1)).createSignedTransaction(requestCaptor.capture());
    TransactionDto request = requestCaptor.getValue();
    assertEquals("verify reward transaction request contains a valid public key", MASTER_KEY,
        request.getPublicKey());
    verify(wallet).calculateBalance(matches(MASTER_KEY));
    
    assertNotNull("verify transactions is not null", transactionsWithReward);
    assertEquals("verify that it contains the reward transaction", 1, transactionsWithReward.size());
    assertFalse("verify reward transaction has inputs", transactionsWithReward.get(0).getInputs().isEmpty());
    TransactionInput rewardInput = transactionsWithReward.get(0).getInputs().get(0);
    assertTrue("verify reward transaction input amount",
        new BigDecimal("100").compareTo(rewardInput.getAmount()) == 0);
    assertEquals("verify reward transaction input key", MASTER_KEY, rewardInput.getPublicKey());
    assertEquals("verify reward transaction input sequence nr.", 1l,
        rewardInput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction input has timestamp", rewardInput.getTimestamp());
    TransactionInput balanceInput = transactionsWithReward.get(0).getInputs().get(1);
    assertTrue("verify balance transaction input amount",
        new BigDecimal("122").compareTo(balanceInput.getAmount()) == 0);
    assertEquals("verify balance transaction input key", MASTER_KEY, balanceInput.getPublicKey());
    assertEquals("verify balance transaction input sequence nr.", 2l,
        balanceInput.getSequenceNumber().longValue());
    assertNotNull("verify balance transaction input has timestamp", balanceInput.getTimestamp());
    
    assertFalse("verify reward transaction has outputs",
        transactionsWithReward.get(0).getOutputs().isEmpty());
    TransactionOutput rewardOutput = transactionsWithReward.get(0).getOutputs().get(0);
    assertTrue("verify reward transaction output amount",
        new BigDecimal("100").compareTo(rewardOutput.getAmount()) == 0);
    assertEquals("verify reward transaction output key", MASTER_KEY, rewardOutput.getPublicKey());
    assertEquals("verify reward transaction output sequence nr.", 1l,
        rewardOutput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction output has timestamp", rewardOutput.getTimestamp());
    TransactionOutput balanceOutput = transactionsWithReward.get(0).getOutputs().get(1);
    assertTrue("verify balance transaction output amount",
        new BigDecimal("122").compareTo(balanceOutput.getAmount()) == 0);
    assertEquals("verify balance transaction output key", MASTER_KEY, balanceOutput.getPublicKey());
    assertEquals("verify balance transaction output sequence nr.", 2l,
        balanceOutput.getSequenceNumber().longValue());
    assertNotNull("verify balance transaction output has timestamp", balanceOutput.getTimestamp());
  }
  
  @Test
  public void testGenerateRewardTransaction_withEmptyTransactionsAndEmptyWallet_shouldAddRewardButNoBalance() {
    when(wallet.calculateBalance(anyString())).thenReturn(BigDecimal.ZERO);
    
    var transactionsWithReward = generator.generateRewardTransaction(new ArrayList<>());

    ArgumentCaptor<TransactionDto> requestCaptor = ArgumentCaptor.forClass(TransactionDto.class);
    verify(transactionManager,times(1)).createSignedTransaction(requestCaptor.capture());
    TransactionDto request = requestCaptor.getValue();
    assertEquals("verify reward transaction request contains a valid public key", MASTER_KEY,
        request.getPublicKey());
    verify(wallet).calculateBalance(matches(MASTER_KEY));
    
    assertNotNull("verify transactions is not null", transactionsWithReward);
    assertEquals("verify that it contains the reward transaction", 1, transactionsWithReward.size());
    assertFalse("verify reward transaction has inputs", transactionsWithReward.get(0).getInputs().isEmpty());
    TransactionInput rewardInput = transactionsWithReward.get(0).getInputs().get(0);
    assertTrue("verify reward transaction input amount",
        new BigDecimal("100").compareTo(rewardInput.getAmount()) == 0);
    assertEquals("verify reward transaction input key", MASTER_KEY, rewardInput.getPublicKey());
    assertEquals("verify reward transaction input sequence nr.", 1l,
        rewardInput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction input has timestamp", rewardInput.getTimestamp());
    
    assertFalse("verify reward transaction has outputs",
        transactionsWithReward.get(0).getOutputs().isEmpty());
    TransactionOutput rewardOutput = transactionsWithReward.get(0).getOutputs().get(0);
    assertTrue("verify reward transaction output amount",
        new BigDecimal("100").compareTo(rewardOutput.getAmount()) == 0);
    assertEquals("verify reward transaction output key", MASTER_KEY, rewardOutput.getPublicKey());
    assertEquals("verify reward transaction output sequence nr.", 1l,
        rewardOutput.getSequenceNumber().longValue());
    assertNotNull("verify reward transaction output has timestamp", rewardOutput.getTimestamp());
    
    assertEquals("verify that the reward transaction doesn't contain a balance input", 
        1, transactionsWithReward.get(0).getInputs().size());
    assertEquals("verify that the reward transaction doesn't contain a balance output", 
        1, transactionsWithReward.get(0).getOutputs().size());
  }
}
