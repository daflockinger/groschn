package com.flockinger.groschn.blockchain.wallet.impl;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionInput;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionOutput;
import com.flockinger.groschn.blockchain.wallet.WalletService;

public class WalletServiceImpl implements WalletService {

  @Autowired
  private BlockchainRepository blockDao;

  @Override
  public String getPublicKey() {
    // TODO implement
    return null;
  }

  @Override
  public PrivateKey getPrivateKey() {
    // TODO implement
    return null;
  }

  @Override
  public BigDecimal calculateBalance(String publicKey) {
    var firstExpense =
        blockDao.findFirstByTransactionsInputsPublicKeyOrderByPositionDesc(publicKey);
    long outputScannerStartPosition = 0;
    BigDecimal balance = BigDecimal.ZERO;

    if (firstExpense.isPresent()) {
      outputScannerStartPosition = firstExpense.get().getPosition() + 1;
      balance = getCorrectExpenseBlockBalance(firstExpense.get(), publicKey);
    }
    var incomeBlocks = blockDao.findByPositionGreaterThanEqualAndTransactionsOutputsPublicKey(
            outputScannerStartPosition, publicKey);
    var incomeTransactions = incomeBlocks.stream().map(StoredBlock::getTransactions).flatMap(Collection::stream);
    balance = balance.add(totalAmount(incomeTransactions, publicKey));
    return balance;
  }
  
  private BigDecimal totalAmount(Stream<StoredTransaction> transactions, String publicKey) {
    return transactions
        .map(StoredTransaction::getOutputs)
        .flatMap(Collection::stream)
        .filter(output -> StringUtils.equals(output.getPublicKey(), publicKey))
        .map(StoredTransactionOutput::getAmount)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
  }
  
  private BigDecimal getCorrectExpenseBlockBalance(StoredBlock block, String publicKey) {
    Optional<StoredTransaction> latestTransaction = block.getTransactions().stream()
      .filter(tx -> containsPubKeyOutput(tx, publicKey))
      .reduce((firstTx,secondTx) -> getLatestTransaction(firstTx, secondTx, publicKey));
    return totalAmount(latestTransaction.stream(), publicKey);
  }
  
  private boolean containsPubKeyOutput(StoredTransaction transaction, String publicKey) {
    return transaction.getOutputs().stream()
        .anyMatch(output -> StringUtils.equals(output.getPublicKey(), publicKey));
  }
  
  private StoredTransaction getLatestTransaction(StoredTransaction firstTx, StoredTransaction secondTx, String publicKey) {
    return (latestTxInputTimeStamp(firstTx, publicKey) > latestTxInputTimeStamp(secondTx, publicKey)) 
        ? firstTx : secondTx;
  }
  
  public Long latestTxInputTimeStamp(StoredTransaction transaction, String publicKey) {
    return transaction.getInputs().stream()
      .filter(input -> StringUtils.equals(input.getPublicKey(),publicKey))
      .map(StoredTransactionInput::getTimestamp)
      .reduce((firstStamp, secondStamp) -> (firstStamp > secondStamp)
          ? firstStamp : secondStamp).orElse(0l);
  }
}
