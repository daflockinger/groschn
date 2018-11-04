package com.flockinger.groschn.blockchain.blockworks.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.RewardGenerator;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.dto.TransactionStatementDto;
import com.flockinger.groschn.blockchain.exception.validation.transaction.TransactionException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.transaction.Bookkeeper;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionUtils;
import com.flockinger.groschn.blockchain.wallet.WalletService;

@Component
public class RewardGeneratorImpl implements RewardGenerator {

  @Autowired
  private TransactionManager transactionManager;
  @Autowired
  private Bookkeeper bookkeeper;
  @Autowired
  private WalletService wallet;
  
  private final static Logger LOG = LoggerFactory.getLogger(RewardGenerator.class);
  
  @Override
  public List<Transaction> generateRewardTransaction(List<Transaction> blockTransactions) {
    var transactionsWithReward = new ArrayList<Transaction>();
    transactionsWithReward.addAll(blockTransactions);
    String publicKey = wallet.getNodePublicKey();
    TransactionDto baseRewardTransaction = createBaseRewardTransaction(transactionsWithReward, publicKey);
    TransactionDto rewardTransaction = new TransactionDto();
    rewardTransaction.getInputs().addAll(baseRewardTransaction.getInputs());
    rewardTransaction.getOutputs().addAll(baseRewardTransaction.getOutputs());
    rewardTransaction.setPublicKey(publicKey);
    
    if (!hasExpenseTransaction(transactionsWithReward, publicKey)) { 
      addBalanceInAndOutput(rewardTransaction);
    }
    transactionsWithReward.add(transactionManager.createSignedTransaction(rewardTransaction));
    return transactionsWithReward;
  }

  private TransactionDto createBaseRewardTransaction(List<Transaction> transactions,
      String publicKey) {
    BigDecimal totalChange = transactions.stream().map(this::countTransactionSave)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    BigDecimal reward = bookkeeper.calculateCurrentBlockReward();
    TransactionStatementDto rewardInput = createTransactionStatement(reward, 1l, publicKey);
    List<TransactionStatementDto> outputs = new ArrayList<>();
    outputs.add(createTransactionStatement(reward, 1l, publicKey));
    if (totalChange.compareTo(BigDecimal.ZERO) > 0) {
      outputs.add(createTransactionStatement(totalChange, 2l, publicKey));
    }
    TransactionDto transaction = new TransactionDto();
    transaction.getInputs().add(rewardInput);
    transaction.setOutputs(outputs);
    return transaction;
  }

  private BigDecimal countTransactionSave(Transaction transaction) {
    BigDecimal change = BigDecimal.ZERO;
    try {
      change = bookkeeper.countChange(transaction);
    } catch (TransactionException e) {
      LOG.warn("Skip transaction for change calculation cause it's invalid for it!", e);
    }
    return change;
  }

  private TransactionStatementDto createTransactionStatement(BigDecimal amount, long sequenceNumber,
      String publicKey) {
    TransactionStatementDto output = new TransactionStatementDto();
    output.setAmount(amount.doubleValue());
    output.setSequenceNumber(sequenceNumber);
    output.setPublicKey(publicKey);
    output.setTimestamp(new Date().getTime());
    return output;
  }

  private boolean hasExpenseTransaction(List<Transaction> transactions,
      String publicKey) {
    TransactionUtils utils = TransactionUtils.build(publicKey);
    return utils.findLatestExpenseTransaction(transactions).isPresent();
  }

  private void addBalanceInAndOutput(TransactionDto transaction) {
    final String publicKey = wallet.getNodePublicKey();
    var balance = wallet.calculateBalance(publicKey);
    if(balance.compareTo(BigDecimal.ZERO) > 0) {
      transaction.getInputs().add(createTransactionStatement(balance, 2l, publicKey));
      transaction.getOutputs().add(createTransactionStatement(
          balance, transaction.getOutputs().size() + 1, publicKey));
    }
  }
}
