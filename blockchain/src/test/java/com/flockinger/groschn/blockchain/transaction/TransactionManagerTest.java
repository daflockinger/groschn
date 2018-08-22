package com.flockinger.groschn.blockchain.transaction;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionManagerImpl;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionPoolListener;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.messaging.distribution.DistributedCollectionBuilder;

@ContextConfiguration(classes = {TransactionManagerImpl.class, TransactionPoolRepository.class, BlockchainRepository.class})
public class TransactionManagerTest extends BaseDbTest {

  @Autowired
  private TransactionManager manager;
  
  @MockBean
  private DistributedCollectionBuilder distributedCollectionBuilder;
  @MockBean
  private TransactionPoolListener transactionListener;
  @MockBean(name="ECDSA_Signer")
  private Signer signer;
  
  @Before
  public void setup() {
    when(distributedCollectionBuilder.createSetWithListener(any(), anyString())).thenReturn(null);
  }
  
  @Test
  public void test_with_should() {}
  
  
  /*
   TODO write tests
   
   @Test
  public void test_with_should() {}
  
  List<Transaction> fetchTransactionsFromPool(long maxByteSize);

  Optional<TransactionOutput> findTransactionFromPointCut(TransactionPointCut pointCut);
  
  Transaction createSignedTransaction(TransactionDto transactionSigningRequest);
   
   
   * */
}
