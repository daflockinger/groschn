package com.flockinger.groschn.blockchain.transaction.impl;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.exception.validation.transaction.TransactionInputMissingOutputBalanceException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.model.TransactionPointCut;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionOutput;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.HashUtils;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.messaging.distribution.DistributedCollectionBuilder;
import com.flockinger.groschn.messaging.distribution.DistributedExternalSet;

@Component
public class TransactionManagerImpl implements TransactionManager {

  @Autowired
  private TransactionPoolRepository transactionDao;
  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private ModelMapper mapper;
  @Autowired
  private DistributedCollectionBuilder distributedCollectionBuilder;
  @Autowired
  private TransactionPoolListener transactionListener;
  @Autowired
  @Qualifier("ECDSA_Signer")
  private Signer signer;
  @Autowired
  private CompressionUtils compressor;
  @Autowired
  private WalletService wallet;
  @Autowired
  private HashGenerator hashGenerator;
  
  private DistributedExternalSet<Transaction> externalTransactions;
  
  @PostConstruct
  public void initDistributedTransactions() {
    externalTransactions = distributedCollectionBuilder.createSetWithListener(transactionListener, "transactionPool");
  }
  

  @Override
  public List<Transaction> fetchTransactionsFromPool(long maxByteSize) {
    var transactionIterator = 
        transactionDao.findByStatusOrderByCreatedAtDesc(TransactionStatus.RAW).iterator();
    var transactions = new ArrayList<Transaction>();
    var compressedTransactionsSize = 0l;
    
    while(compressedTransactionsSize < maxByteSize && transactionIterator.hasNext()) {
      var freshTransaction = mapToRegularTransaction(transactionIterator.next());
      compressedTransactionsSize += getSize(freshTransaction);
      if(compressedTransactionsSize < maxByteSize) {
        transactions.add(freshTransaction);
      }
    }
    return transactions;
  }

  private long getSize(Transaction transaction) {
    return compressor.compress(transaction).getEntity().length;
  }

  private Transaction mapToRegularTransaction(StoredPoolTransaction poolTransaction) {
    return mapper.map(poolTransaction, Transaction.class);
  }
  

  @Override
  public Optional<TransactionOutput> findTransactionFromPointCut(TransactionPointCut pointCut) {
    var transactionHash = pointCut.getTransactionHash();
    var storedBlock = blockDao.findByTransactionsTransactionHash(transactionHash);
    Optional<TransactionOutput> foundTransaction = Optional.empty();

    if (storedBlock.isPresent()) {
      foundTransaction = storedBlock.get().getTransactions().stream()
          .filter(
              transaction -> StringUtils.equals(transaction.getTransactionHash(), transactionHash))
          .map(StoredTransaction::getOutputs)
          .filter(Objects::nonNull)
          .flatMap(Collection::stream)
          .filter(output -> output.getSequenceNumber() == pointCut.getSequenceNumber())
          .map(this::mapToRegularOutput).findAny();
    }
    return foundTransaction;
  }

  private TransactionOutput mapToRegularOutput(StoredTransactionOutput storedOutput) {
    return mapper.map(storedOutput, TransactionOutput.class);
  }


  @Override
  public Transaction createSignedTransaction(TransactionDto transactionSigningRequest) {
    var transaction = mapper.map(transactionSigningRequest, Transaction.class);
    var walletPrivateKey = wallet.getPrivateKey(transactionSigningRequest.getPublicKey(), 
        transactionSigningRequest.getSecretWalletKey());
    for(TransactionInput input: transaction.getInputs()) {
      signTransactionInput(input, transaction.getOutputs(), walletPrivateKey);
    }
    transaction.setTransactionHash(hashGenerator.generateHash(transaction));
    //TODO maybe find better way to generate entity ID's
    transaction.setId(UUID.randomUUID().toString());
    return transaction;
  }
  
  private void signTransactionInput(TransactionInput input, List<TransactionOutput> outputs, PrivateKey privateKey) {
     input.setPreviousOutputTransaction(createPointcut(input.getPublicKey()));
     String signature = signer.sign(HashUtils.toByteArray(outputs), privateKey);
     input.setSignature(signature);
  }
  
  private TransactionPointCut createPointcut(String publicKey) {
    var latestTransaction = findLatestWithOutputFrom(publicKey);
    var pointcut = new TransactionPointCut();
    pointcut.setTransactionHash(latestTransaction.getTransactionHash());
    
    long outputSequenceNumber = latestTransaction.getOutputs().stream().filter(output -> 
    StringUtils.equals(publicKey,output.getPublicKey()))
        .map(StoredTransactionOutput::getSequenceNumber).findFirst().get();
    pointcut.setSequenceNumber(outputSequenceNumber);
    return pointcut;
  }
  
  private StoredTransaction findLatestWithOutputFrom(String publicKey) {
    var block = blockDao.findFirstByTransactionsOutputsPublicKeyOrderByPositionDesc(publicKey);
    Optional<StoredTransaction> lastTransaction = block.stream()
      .map(StoredBlock::getTransactions)
      .flatMap(Collection::stream)
      .filter(transaction -> transaction.getOutputs().stream()
          .anyMatch(output -> StringUtils.equals(publicKey,output.getPublicKey())))
      .findFirst();
    if(!lastTransaction.isPresent()) {
      throw new TransactionInputMissingOutputBalanceException("Public key "
          + "has no balance (output statement) on the blockchain with with address: " + publicKey);
    }
    return lastTransaction.get();
  }
}
