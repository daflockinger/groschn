package com.flockinger.groschn.blockchain.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static com.flockinger.groschn.blockchain.TestDataFactory.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionUtils;

public class TransactionUtilsTest {
  
  private final static String MASTER_KEY = "master-key";
  private TransactionUtils utils = TransactionUtils.build(MASTER_KEY);
  
  @Test
  public void testFindLatestExpenseTransaction_withMultipleOutputTransactions_shouldSelectCorrectOne() {
    List<Transaction> transactions = new ArrayList<>();
    
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, 
        createRandomTransactionOutputWith(2, MASTER_KEY, 123l, 2000l), 
        createRandomTransactionInputWith(2, MASTER_KEY, 125l, 2000l))));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, createRandomTransactionOutputWith(2, MASTER_KEY, 5000l, 1000l), null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, createRandomTransactionOutputWith(2, MASTER_KEY, 6000l, 3000l), null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, createRandomTransactionOutputWith(2, MASTER_KEY, 7000l, 2001l), null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));

    Optional<Transaction> latestOne = utils.findLatestExpenseTransaction(transactions);
    
    assertTrue("verify latest expense transaction exists", latestOne.isPresent());
    assertTrue("verify latest transaction has an input", latestOne.get().getInputs().stream()
        .anyMatch(input -> input.getPublicKey().equals(MASTER_KEY)));
    Optional<TransactionOutput> correctOutput = latestOne.get().getOutputs().stream()
        .filter(output -> output.getPublicKey().equals(MASTER_KEY)).findFirst();
    assertTrue("verify that transaction contains a key output", correctOutput.isPresent());
    assertTrue("verify that output has the correct amount", 
        correctOutput.get().getAmount().compareTo(new BigDecimal("123")) == 0);
  }
  
  @Test
  public void testFindLatestExpenseTransaction_withOnlyOutputTransactions_shouldReturnEmpty() {
    List<Transaction> transactions = new ArrayList<>();
    
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, createRandomTransactionOutputWith(2, MASTER_KEY, 5000l, 1000l), null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, createRandomTransactionOutputWith(2, MASTER_KEY, 6000l, 3000l), null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, createRandomTransactionOutputWith(2, MASTER_KEY, 7000l, 2001l), null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));

    Optional<Transaction> latestOne = utils.findLatestExpenseTransaction(transactions);
    
    assertFalse("verify latest expense transaction doesn't exist", latestOne.isPresent());
  }
  
  @Test
  public void testFindLatestExpenseTransaction_withOtherRandomTransactions_shouldReturnEmpty() {
    List<Transaction> transactions = new ArrayList<>();
    
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));
    transactions.add(mapToTransaction(createRandomTransactionWith(null, null, null)));

    Optional<Transaction> latestOne = utils.findLatestExpenseTransaction(transactions);
    
    assertFalse("verify latest expense transaction doesn't exist", latestOne.isPresent());
  }
  
  @Test
  public void testFindLatestExpenseTransaction_withEmptyTransactions_shouldReturnEmpty() {
    List<Transaction> transactions = new ArrayList<>();

    Optional<Transaction> latestOne = utils.findLatestExpenseTransaction(transactions);
    
    assertFalse("verify latest expense transaction doesn't exist", latestOne.isPresent());
  }
}
