package com.flockinger.groschn.blockchain.wallet.impl;

import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.dto.UnlockedWalletDto;
import com.flockinger.groschn.blockchain.dto.WalletDto;
import com.flockinger.groschn.blockchain.exception.wallet.WalletNotFoundException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.WalletRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredWallet;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionUtils;
import com.flockinger.groschn.blockchain.util.Base58;
import com.flockinger.groschn.blockchain.util.crypto.EncryptedKey;
import com.flockinger.groschn.blockchain.util.crypto.KeyCipher;
import com.flockinger.groschn.blockchain.util.sign.Signer;
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
  @Qualifier("ECDSA_Signer")
  private Signer signer;
  @Autowired
  private WalletRepository walletDao;
  
  @Value("${blockchain.node.credentials.public-key}")
  private String nodePublicKey;
  @Value("${blockchain.node.credentials.private-key}")
  private String nodePrivateKey;
  
  @Override
  public String getNodePublicKey() {
    return nodePublicKey;
  }

  @Override
  public byte[] getPrivateKey(String publicKey, String walletEncryptionKey) {
    byte[] privateKey = new byte[0];
    Optional<StoredWallet> possibleWallet = walletDao.findByPublicKey(publicKey);
    if(possibleWallet.isPresent()) {
      StoredWallet wallet = possibleWallet.get();
      byte[] encryptionKey = Base58.decode(walletEncryptionKey);
      privateKey = cipher.decrypt(EncryptedKey.build()
          .key(wallet.getEncryptedPrivateKey()).initVector(wallet.getInitVector()),encryptionKey);
    } else if (publicKey.equals(nodePublicKey)){ 
      privateKey = Base58.decode(nodePrivateKey);
    } else {
      throw new WalletNotFoundException("No wallet found with public key: " + publicKey);
    }
    return privateKey;
  }


  @Override
  public WalletDto createWallet() {
    byte[] passphrase = cipher.createPassphrase();
    KeyPair keys = signer.generateKeyPair();
    String publicKey = Base58.encode(keys.getPublic().getEncoded());
    EncryptedKey encryptedPrivateKey = cipher.encrypt(keys.getPrivate().getEncoded(), passphrase);
    
    saveWallet(encryptedPrivateKey, publicKey);
    
    return WalletDto.build().publicKey(publicKey)
        .walletEncryptionKey(Base58.encode(passphrase));
  }
  
  private void saveWallet(EncryptedKey encryptedPrivateKey, String publicKey) {
    StoredWallet storedWallet = new StoredWallet();
    storedWallet.setEncryptedPrivateKey(encryptedPrivateKey.getKey());
    storedWallet.setInitVector(encryptedPrivateKey.getInitVector());
    storedWallet.setPublicKey(publicKey);
    walletDao.save(storedWallet);
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
