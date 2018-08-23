package com.flockinger.groschn.blockchain.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockMakerImpl;
import com.flockinger.groschn.blockchain.blockworks.impl.MultiHashGenerator;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfMajorityAlgorithm;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.repository.BlockProcessRepository;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.WalletRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.transaction.impl.BookkeeperImpl;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionManagerImpl;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionPoolListener;
import com.flockinger.groschn.blockchain.util.CompressedEntity;
import com.flockinger.groschn.blockchain.util.CompressionUtils;
import com.flockinger.groschn.blockchain.util.MerkleRootCalculator;
import com.flockinger.groschn.blockchain.util.crypto.impl.KeyAESCipher;
import com.flockinger.groschn.blockchain.util.sign.impl.EcdsaSecpSigner;
import com.flockinger.groschn.blockchain.validation.impl.BlockValidator;
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidator;
import com.flockinger.groschn.blockchain.wallet.impl.WalletServiceImpl;
import com.flockinger.groschn.messaging.distribution.DistributedCollectionBuilder;
import com.flockinger.groschn.messaging.members.ElectionStatistics;
import com.flockinger.groschn.messaging.outbound.Broadcaster;

@ContextConfiguration(classes = {BlockValidator.class, BlockchainRepository.class, 
    MultiHashGenerator.class, MerkleRootCalculator.class, 
    // those are all needed to create a somewhat real block to verify:
    BlockMakerImpl.class,
    ConsensusFactory.class, ProofOfWorkAlgorithm.class, ProofOfMajorityAlgorithm.class,
    TransactionManagerImpl.class, TransactionPoolRepository.class, EcdsaSecpSigner.class, 
    BlockProcessRepository.class, BookkeeperImpl.class, WalletServiceImpl.class, WalletRepository.class, 
    KeyAESCipher.class}, initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(locations="classpath:application.yml")
public class BlockValidatorTest extends BaseDbTest {

  @MockBean
  private TransactionValidator transactionMockator;
  @MockBean
  private ConsentValidator consentMockator;
  @MockBean
  private CompressionUtils compressor;
  
  @MockBean
  private DistributedCollectionBuilder distributedCollectionBuilder;
  @MockBean
  private TransactionPoolListener transactionListener;
  @MockBean
  private ElectionStatistics statistics;
  @MockBean
  private BlockStorageService storageService;
  @MockBean
  private Broadcaster<CompressedEntity> broadcaster;
  
  
  @Autowired
  private BlockValidator validator;
  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private ModelMapper mapper;
  
  @Autowired
  private BlockMaker maker;
  
  private Block freshBlock;
 
  //TODO add many more tests that cover all the failure possibilities
  /*
   Test about those failures:
   * 
   * 1. check if position is higher than existing one
   * 2. check if lastHash is correct 
   * 3. verify if current hash is correctly calculated
   * 4. check if transaction merkleRoot-Hash is correct
   * 5. check if timestamp is in the past but not too much (set limit for that maybe 2 hours like bitcoin or less)
   * 6. check if version is correct
   * 7. check max transaction size
   * 8. call consent validation
   * 9. call transaction validations
   */
  
  @Before
  public void setup() {    
    when(consentMockator.type()).thenReturn(ConsensusType.PROOF_OF_WORK);
    
    if(freshBlock == null) {
      when(storageService.getLatestBlockPosition()).thenReturn(1l);
      blockDao.save(mapper.map(Block.GENESIS_BLOCK(), StoredBlock.class));
      
      maker.produceBlock();
      ArgumentCaptor<Block> blockCaptor = ArgumentCaptor.forClass(Block.class);
      verify(storageService).saveInBlockchain(blockCaptor.capture());
      freshBlock = blockCaptor.getValue();
    }
  }
  
  @Test
  public void testValidate_withValidBigBlock_shouldValidateCorrectly() {
    when(compressor.compressedByteSize(anyList())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    when(transactionMockator.validate(any())).thenReturn(Assessment.build().valid(true));
    when(consentMockator.validate(any())).thenReturn(Assessment.build().valid(true));

    Assessment result = validator.validate(freshBlock);
    
    assertEquals("verify that correct mined reward block is VALID", true, result.isValid());
  }
  
  
}
