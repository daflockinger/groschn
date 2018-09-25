package com.flockinger.groschn.blockchain.transaction.impl;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.exception.TransactionAlreadyClearedException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.commons.compress.CompressionUtils;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.sign.Signer;
import com.google.common.collect.ImmutableList;

@Service
public class TransactionManagerImpl implements TransactionManager {

  @Autowired
  private TransactionPoolRepository transactionDao;
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
  private BlockSerializer serializer;

  @Autowired
  public TransactionManagerImpl(MongoDbFactory factory) {
    template = new MongoTemplate(factory);
  }

  @Override
  public List<Transaction> fetchTransactionsBySize(long maxByteSize) {
    var transactionIterator =
        transactionDao.findByStatusOrderByCreatedAtAsc(TransactionStatus.RAW).iterator();
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
    String signature = signer.sign(serializer.serialize(outputs), privateKey);
    input.setSignature(signature);
  }


  @Override
  public void storeTransaction(Transaction transaction) {
    Assessment assessment = validator.validate(transaction);
    if (!assessment.isValid()) {
      throw new AssessmentFailedException(assessment.getReasonOfFailure());
    }
    if (transactionDao.existsByTransactionHash(transaction.getTransactionHash())) {
      throw new TransactionAlreadyClearedException("Transaction already exists in pool!");
    }
    StoredPoolTransaction toStoreTransaction = mapToStoredPoolTransaction(transaction);
    toStoreTransaction.setCreatedAt(new Date());
    toStoreTransaction.setStatus(TransactionStatus.RAW);
    transactionDao.save(toStoreTransaction);
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
        .findByStatusOrderByCreatedAtAsc(TransactionStatus.RAW, PageRequest.of(abs(page), perfectSize));
    return transactions.stream().map(this::mapToRegularTransaction).collect(Collectors.toList());
  }
}
