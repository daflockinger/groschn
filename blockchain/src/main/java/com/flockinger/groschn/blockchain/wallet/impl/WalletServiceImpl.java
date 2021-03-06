package com.flockinger.groschn.blockchain.wallet.impl;

import com.flockinger.groschn.blockchain.dto.WalletDto;
import com.flockinger.groschn.blockchain.exception.wallet.WalletNotFoundException;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.WalletRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredWallet;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.commons.crypto.EncryptedKey;
import com.flockinger.groschn.commons.crypto.KeyCipher;
import com.flockinger.groschn.commons.hash.Base58;
import com.flockinger.groschn.commons.sign.Signer;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WalletServiceImpl implements WalletService {

  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private ModelMapper mapper;
  @Autowired
  private KeyCipher cipher;
  @Autowired
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
    long outputScannerStartPosition = getLastExpenseBlockPosition(publicKey);
    var incomeBlocks = findIncomeBlocks(outputScannerStartPosition, publicKey);
    var incomeTransactions = incomeBlocks.stream().map(Block::getTransactions).flatMap(Collection::stream);
    return totalAmount(incomeTransactions, publicKey);
  }
  
  private Long getLastExpenseBlockPosition(String publicKey) {
    Optional<Block> lastExpense = blockDao.findFirstByTransactionsInputsPublicKeyOrderByPositionDesc(publicKey).stream()
        .map(this::mapToBlock).findFirst();
    return lastExpense.stream().map(Block::getPosition)
        .findFirst().orElse(0l);
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
  
  private Block mapToBlock(StoredBlock storedBlock) {
    return mapper.map(storedBlock, Block.class);
  }
}
