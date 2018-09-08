package com.flockinger.groschn.blockchain.transaction.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.dto.TransactionDto;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredPoolTransaction;
import com.flockinger.groschn.blockchain.repository.model.TransactionStatus;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.serialize.BlockSerializer;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.blockchain.validation.Validator;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import com.flockinger.groschn.messaging.distribution.DistributedCollectionBuilder;
import com.flockinger.groschn.messaging.distribution.DistributedExternalSet;
import com.google.common.collect.ImmutableList;

@Component
public class TransactionManagerImpl implements TransactionManager {

  @Autowired
  private TransactionPoolRepository transactionDao;
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
  @Autowired
  @Qualifier("Transaction_Validator")
  private Validator<Transaction> validator;
  @Autowired
  private BlockSerializer serializer;

  private DistributedExternalSet<Transaction> externalTransactions;

  @PostConstruct
  public void initDistributedTransactions() {
    externalTransactions =
        distributedCollectionBuilder.createSetWithListener(transactionListener, "transactionPool");
  }


  @Override
  public List<Transaction> fetchTransactionsFromPool(long maxByteSize) {
    var transactionIterator =
        transactionDao.findByStatusOrderByCreatedAtDesc(TransactionStatus.RAW).iterator();
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


  /*
   * TODO maybe check if a Transaction from the same input publicKey is already in the pool, 
   * but not yet added to the chain that the new one is revoked until the old one is processed, 
   * to ensure that the new one is not kicked later one (cause the input amount value is wrong).
   */
  @Override
  public Transaction createSignedTransaction(TransactionDto transactionSigningRequest) {
    var transaction = mapper.map(transactionSigningRequest, Transaction.class);
    var walletPrivateKey = wallet.getPrivateKey(transactionSigningRequest.getPublicKey(),
        transactionSigningRequest.getSecretWalletKey());
    for (TransactionInput input : transaction.getInputs()) {
      signTransactionInput(input, transaction.getOutputs(), walletPrivateKey);
    }
    transaction.setTransactionHash(hashGenerator.generateHash(transaction));
    transaction.setId(UUID.randomUUID().toString());
    validator.validate(transaction);
    return transaction;
  }

  private void signTransactionInput(TransactionInput input, List<TransactionOutput> outputs,
      byte[] privateKey) {
    String signature = signer.sign(serializer.serialize(outputs), privateKey);
    input.setSignature(signature);
  }
}
