package com.flockinger.groschn.blockchain.transaction.impl;

import static com.flockinger.groschn.blockchain.repository.model.TransactionStatus.RAW;
import static com.flockinger.groschn.blockchain.repository.model.TransactionStatus.SIX_BLOCKS_UNDER;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import com.flockinger.groschn.blockchain.api.dto.TransactionIdDto;
import com.flockinger.groschn.blockchain.api.dto.TransactionStatusDto;
import com.flockinger.groschn.blockchain.api.dto.ViewTransactionDto;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.exception.TransactionAlreadyClearedException;
import com.flockinger.groschn.blockchain.exception.TransactionNotFoundException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.commons.compress.CompressionUtils;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;
import com.google.common.collect.ImmutableList;

@Service
public class TransactionManagerImpl implements TransactionManager {

  @Autowired
  private TransactionPoolRepository transactionDao;
  @Autowired
  private BlockchainRepository blockchainDao;
  private final MongoTemplate template;
  @Autowired
  private ModelMapper mapper;
  @Autowired
  private Signer signer;
  @Autowired
  private CompressionUtils compressor;
  @Autowired
  private WalletService wallet;
  @Autowired
  private HashGenerator hashGenerator;
  @Autowired
  @Qualifier("Transaction_Validator")
  private Validator<Transaction> validator;

  @Autowired
  public TransactionManagerImpl(MongoDbFactory factory) {
    template = new MongoTemplate(factory);
  }

  @Override
  public List<Transaction> fetchTransactionsBySize(long maxByteSize) {
    var transactionIterator =
        transactionDao.findByStatusOrderByCreatedAtAsc(RAW).iterator();
    var transactions = new ArrayList<Transaction>();
    var compressedTransactionsSize = 0l;

    while (compressedTransactionsSize < maxByteSize && transactionIterator.hasNext()) {
      var freshTransaction = mapToRegularTransaction(transactionIterator.next());
      compressedTransactionsSize +=
          compressor.compressedByteSize(ImmutableList.of(freshTransaction));
      if (compressedTransactionsSize < maxByteSize) {
        transactions.add(freshTransaction);
      }
    }
    return transactions;
  }

  private Transaction mapToRegularTransaction(StoredPoolTransaction poolTransaction) {
    return mapper.map(poolTransaction, Transaction.class);
  }


  @Override
  public Transaction createSignedTransaction(TransactionDto transactionSigningRequest) {
    var transaction = mapper.map(transactionSigningRequest, Transaction.class);
    var walletPrivateKey = wallet.getPrivateKey(transactionSigningRequest.getPublicKey(),
        transactionSigningRequest.getSecretWalletKey());
    for (TransactionInput input : transaction.getInputs()) {
      signTransactionInput(input, transaction.getOutputs(), walletPrivateKey);
    }
    transaction.setTransactionHash(hashGenerator.generateHash(transaction));
    validator.validate(transaction);
    return transaction;
  }

  private void signTransactionInput(TransactionInput input, List<TransactionOutput> outputs,
      byte[] privateKey) {
    Collections.sort(outputs);
    String outputHashBase = outputs.stream().map(Object::toString).collect(Collectors.joining());
    String signature = signer.sign(outputHashBase.getBytes(StandardCharsets.UTF_8), privateKey);
    input.setSignature(signature);
  }


  @Override
  public TransactionIdDto storeTransaction(Transaction transaction) {
    Assessment assessment = validator.validate(transaction);
    if (!assessment.isValid()) {
      throw new AssessmentFailedException(assessment.getReasonOfFailure());
    }
    if (transactionDao.existsByTransactionHash(transaction.getTransactionHash())) {
      throw new TransactionAlreadyClearedException("Transaction already exists in pool!");
    }
    StoredPoolTransaction toStoreTransaction = mapToStoredPoolTransaction(transaction);
    toStoreTransaction.setCreatedAt(new Date());
    toStoreTransaction.setStatus(RAW);
    transactionDao.save(toStoreTransaction);
    return new TransactionIdDto().id(transaction.getTransactionHash());
  }

  private StoredPoolTransaction mapToStoredPoolTransaction(Transaction transaction) {
    return mapper.map(transaction, StoredPoolTransaction.class);
  }
  
  @Override
  public void updateTransactionStatuses(List<Transaction> transactions, TransactionStatus status) {
    List<String> transactionHashes = transactions.stream().map(Transaction::getTransactionHash)
        .filter(Objects::nonNull).collect(Collectors.toList());
    Query whereTransactionHashesIn = new Query();
    whereTransactionHashesIn
        .addCriteria(Criteria.where(StoredPoolTransaction.TX_HASH_NAME).in(transactionHashes));
    Update updatedStatus = Update.update(StoredPoolTransaction.STATUS_NAME, status);
    template.updateMulti(whereTransactionHashesIn, updatedStatus, StoredPoolTransaction.class);
  }

  @Override
  public List<Transaction> fetchTransactionsPaginated(int page, int size) {
    int perfectSize = max(1, abs(size));
    Page<StoredPoolTransaction> transactions = transactionDao
        .findByStatusOrderByCreatedAtAsc(RAW, PageRequest.of(abs(page), perfectSize));
    return transactions.stream().map(this::mapToRegularTransaction).collect(Collectors.toList());
  }

  
  //TODO improve that, more efficient queries, one query instead of 2?
  @Override
  public List<ViewTransactionDto> getTransactionsFromPublicKey(String publicKey) {
    List<ViewTransactionDto> transactions = StreamSupport
                 .stream(transactionDao.findByStatusOrderByCreatedAtAsc(RAW).spliterator(), false)
                 .filter(transaction -> doesTransactionContainPubKey(publicKey, transaction))
                 .map(this::mapPoolTransactionToView)
                 .collect(Collectors.toList());
    transactions.addAll(blockchainDao.findByTransactionsOutputsPublicKey(publicKey)
                 .stream().map(StoredBlock::getTransactions).flatMap(Collection::stream)
                 .filter(transaction -> doesBlockTransactionContainPubKey(publicKey, transaction))
                 .map(this::mapTransactionToView)
                 .collect(Collectors.toList()));
    transactions.addAll(blockchainDao.findByTransactionsInputsPublicKey(publicKey)
                 .stream().map(StoredBlock::getTransactions).flatMap(Collection::stream)
                 .filter(transaction -> doesBlockTransactionContainPubKey(publicKey, transaction))
                 .map(this::mapTransactionToView)
                 .collect(Collectors.toList()));
    return transactions.stream()
        .collect(Collectors.toCollection(() 
            -> new TreeSet<>(Comparator.comparing(ViewTransactionDto::getId))))
        .stream().collect(Collectors.toList());
  }
  
  private boolean doesTransactionContainPubKey(String publicKey, StoredPoolTransaction transaction) {
    return transaction.getOutputs().stream()
              .anyMatch(output -> StringUtils.equals(output.getPublicKey(), publicKey))
        || transaction.getInputs().stream()
              .anyMatch(input -> StringUtils.equals(input.getPublicKey(), publicKey));
  }
  
  private boolean doesBlockTransactionContainPubKey(String publicKey, StoredTransaction transaction) {
    return transaction.getOutputs().stream()
              .anyMatch(output -> StringUtils.equals(output.getPublicKey(), publicKey))
        || transaction.getInputs().stream()
              .anyMatch(input -> StringUtils.equals(input.getPublicKey(), publicKey));
  }
  
  private ViewTransactionDto mapPoolTransactionToView(StoredPoolTransaction poolTransaction) {
    ViewTransactionDto viewTransaction = mapper.map(poolTransaction, ViewTransactionDto.class);
    viewTransaction.setId(poolTransaction.getTransactionHash());
    return viewTransaction;
  }
  
  private ViewTransactionDto mapTransactionToView(StoredTransaction transaction) {
    ViewTransactionDto viewTransaction = mapper.map(transaction, ViewTransactionDto.class);
    viewTransaction.setId(transaction.getTransactionHash());
    return viewTransaction;
  }

  @Override
  public TransactionStatusDto getStatusOfTransaction(String transactionHash)
      throws TransactionNotFoundException {
    TransactionStatusDto status = new TransactionStatusDto();
    Optional<StoredPoolTransaction> poolTransaction = transactionDao.findByTransactionHash(transactionHash);
    poolTransaction.ifPresent(transaction -> { 
      status.setStatus(transaction.getStatus().name());
      status.setStatusMessage(transaction.getStatus().description());
    });
    if(!poolTransaction.isPresent()) {
      Optional<StoredBlock> block = blockchainDao.findByTransactionsTransactionHash(transactionHash);
      filterTransaction(block, transactionHash).ifPresent(transaction -> {
            status.setStatus(SIX_BLOCKS_UNDER.name());
            status.setStatusMessage(SIX_BLOCKS_UNDER.description());
          });
    }
    if(StringUtils.isEmpty(status.getStatus())) {
      throw new TransactionNotFoundException("Couldn't find transaction with hash: " + transactionHash) ;
    }
    return status;
  }
  
  private Optional<StoredTransaction> filterTransaction(Optional<StoredBlock> block, String transactionHash) {
    return block.stream()
        .map(StoredBlock::getTransactions)
        .flatMap(Collection::stream)
        .filter(transaction -> StringUtils.equals(transaction.getTransactionHash(), transactionHash))
        .findFirst();
  }
}
