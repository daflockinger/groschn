package com.flockinger.groschn.blockchain.wallet.impl;

import java.math.BigDecimal;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.dto.WalletDto;
import com.flockinger.groschn.blockchain.dto.WalletSecretDto;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.WalletRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionUtils;
import com.flockinger.groschn.blockchain.util.crypto.KeyCipher;
import com.flockinger.groschn.blockchain.wallet.WalletService;

@Component
public class WalletServiceImpl implements WalletService {

  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private ModelMapper mapper;
  @Autowired
  private KeyCipher cipher;
  @Autowired
  private WalletRepository walletDao;

  @Override
  public String getNodePublicKey() {
    // TODO implement
    return null;
  }

  @Override
  public PrivateKey getPrivateKey(String publicKey, String secretKey) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public WalletDto createWallet() {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public WalletSecretDto fetchAndForgetWalletSecret(String publicKey) {
    // TODO Auto-generated method stub
    return null;
  }
  

  @Override
  public BigDecimal calculateBalance(String publicKey) {
    var firstExpense = getFirstExpense(publicKey);
    long outputScannerStartPosition = 0;
    BigDecimal balance = BigDecimal.ZERO;

    if (firstExpense.isPresent()) {
      outputScannerStartPosition = firstExpense.get().getPosition() + 1;
      balance = getCorrectExpenseBlockBalance(firstExpense.get(), publicKey);
    }
    var incomeBlocks = findIncomeBlocks(outputScannerStartPosition, publicKey);
    var incomeTransactions = incomeBlocks.stream().map(Block::getTransactions).flatMap(Collection::stream);
    balance = balance.add(totalAmount(incomeTransactions, publicKey));
    return balance;
  }
  
  private Optional<Block> getFirstExpense(String publicKey) {
    return blockDao.findFirstByTransactionsInputsPublicKeyOrderByPositionDesc(publicKey).stream()
        .map(this::mapToBlock).findFirst();
  }
  private List<Block> findIncomeBlocks(long outputScannerStartPosition, String publicKey) {
    return blockDao.findByPositionGreaterThanEqualAndTransactionsOutputsPublicKey(
        outputScannerStartPosition, publicKey).stream()
        .map(this::mapToBlock).collect(Collectors.toList());
  }
  
  private BigDecimal totalAmount(Stream<Transaction> transactions, String publicKey) {
    return transactions
        .map(Transaction::getOutputs)
        .flatMap(Collection::stream)
        .filter(output -> StringUtils.equals(output.getPublicKey(), publicKey))
        .map(TransactionOutput::getAmount)
        .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
  }
  
  private BigDecimal getCorrectExpenseBlockBalance(Block block, String publicKey) {
    TransactionUtils utils = TransactionUtils.build(publicKey);
    Optional<Transaction> latestTransaction = block.getTransactions().stream()
      .filter(utils::containsPubKeyOutput)
      .reduce(utils::getLatestTransaction);
    return totalAmount(latestTransaction.stream(), publicKey);
  }
  
  private Block mapToBlock(StoredBlock storedBlock) {
    return mapper.map(storedBlock, Block.class);
  }
  
}
