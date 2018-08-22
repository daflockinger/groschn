package com.flockinger.groschn.blockchain.transaction.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.model.Transaction;
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
import com.flockinger.groschn.blockchain.util.sign.Signer;
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
  
  private DistributedExternalSet<Transaction> externalTransactions;
  
  @PostConstruct
  public void initDistributedTransactions() {
    externalTransactions = distributedCollectionBuilder.createSetWithListener(transactionListener, "transactionPool");
  }
  

  @Override
  public List<Transaction> fetchTransactionsFromPool(long maxByteSize) {
    Iterator<StoredPoolTransaction> transactionIterator = 
        transactionDao.findByStatusOrderByCreatedAtDesc(TransactionStatus.RAW).iterator();
    List<Transaction> transactions = new ArrayList<>();
    long compressedTransactionsSize = 0;
    
    while(compressedTransactionsSize < maxByteSize && transactionIterator.hasNext()) {
      Transaction freshTransaction = mapToRegularTransaction(transactionIterator.next());
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
    String transactionHash = pointCut.getTransactionHash();
    Optional<StoredBlock> storedBlock = blockDao.findByTransactionsTransactionHash(transactionHash);
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
    // TODO create that stuff
    return null;
  }
}
