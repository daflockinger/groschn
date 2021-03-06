package com.flockinger.groschn.blockchain.validation.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.blockworks.BlockMaker;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockGenerationStatus;
import com.flockinger.groschn.blockchain.blockworks.dto.BlockMakerCommand;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockMakerImpl;
import com.flockinger.groschn.blockchain.blockworks.impl.RewardGeneratorImpl;
import com.flockinger.groschn.blockchain.consensus.impl.ConsensusFactory;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfMajorityAlgorithm;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.messaging.sync.GlobalBlockchainStatistics;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockProcessRepository;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.TransactionPoolRepository;
import com.flockinger.groschn.blockchain.repository.WalletRepository;
import com.flockinger.groschn.blockchain.transaction.impl.BookkeeperImpl;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionManagerImpl;
import com.flockinger.groschn.blockchain.transaction.impl.TransactionPoolListener;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.AssessmentFailure;
import com.flockinger.groschn.blockchain.wallet.impl.WalletServiceImpl;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.hash.HashGenerator;
import com.flockinger.groschn.commons.sign.Signer;
import com.flockinger.groschn.messaging.members.NetworkStatistics;
import com.flockinger.groschn.messaging.outbound.Broadcaster;
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@ContextConfiguration(classes = {LastBlockValidator.class, BlockchainRepository.class,
    BlockTransactionsValidator.class, TransactionValidator.class, RewardTransactionValidator.class, 
    TransactionValidationHelper.class, PowConsensusValidator.class,
    // those are all needed to create a somewhat real block to verify:
    BlockMakerImpl.class,
    ConsensusFactory.class, ProofOfWorkAlgorithm.class, ProofOfMajorityAlgorithm.class, RewardGeneratorImpl.class,
    TransactionManagerImpl.class, TransactionPoolRepository.class, BlockProcessRepository.class, 
    BookkeeperImpl.class, WalletServiceImpl.class, WalletRepository.class}, initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(locations="classpath:application.yml")
public class LastBlockValidatorTest extends BaseDbTest {

  //TODO make it faster!!

  @MockBean
  private TransactionPoolListener transactionListener;
  @MockBean
  private NetworkStatistics statistics;
  @MockBean
  private BlockStorageService storageService;
  @MockBean
  private Broadcaster broadcaster;
  @MockBean
  private GlobalBlockchainStatistics blockchainStatistics;

  @Autowired
 private HashGenerator hasher;
  @Autowired
  private Signer signer;
  @MockBean
  private Compressor mockPressor;

  @Autowired
  @Qualifier("lastBlockValidator")
  private LastBlockValidator validator;
  
  @Autowired
  private BlockMaker maker;
  
  private static Block freshBlock;
 
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
    when(storageService.getLatestBlock()).thenReturn(Block.GENESIS_BLOCK());
    when(storageService.getLatestProofOfWorkBlock()).thenReturn(Block.GENESIS_BLOCK());

    if(freshBlock == null) {
      maker.generation(BlockMakerCommand.RESTART);
      ArgumentCaptor<Block> blockCaptor = ArgumentCaptor.forClass(Block.class);
      
      Awaitility.await().atMost(Duration.ONE_MINUTE).until(() -> maker.status().equals(BlockGenerationStatus.COMPLETE));
      
      verify(storageService).saveInBlockchain(blockCaptor.capture());
      freshBlock = blockCaptor.getValue();
    }
    when(blockchainStatistics.overallBlockHashes(any()))
        .thenReturn(ImmutableList.of(freshBlock.getHash()));
  }
  
  @Test
  public void testValidate_withValidBigBlock_shouldValidateSuccess() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);

    Assessment result = validator.validate(freshBlock);
    
    assertEquals("verify that correct mined reward block is VALID", true, result.isValid());
  }
  
  @Test
  public void testValidate_withTooLowPosition_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    freshBlock.setPosition(1l);
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setPosition(2l);
    assertEquals("verify that block with too low position is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"position"));
  }
  
  @Test
  public void testValidate_withTooHighPosition_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    freshBlock.setPosition(3l);
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setPosition(2l);
    assertEquals("verify that block with too high position is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"position"));
    Assert.assertEquals("verify correct AssessmentFailure", AssessmentFailure.BLOCK_POSITION_TOO_HIGH, result.getFailure());
  }
  
  @Test
  public void testValidate_withWayTooHighPosition_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    freshBlock.setPosition(30l);
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setPosition(2l);
    assertEquals("verify that block with too high position is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"position"));
    assertEquals("verify correct AssessmentFailure", AssessmentFailure.BLOCK_POSITION_TOO_HIGH, result.getFailure());
  }
  
  @Test
  public void testValidate_withWrongLastHash_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    String lastHash = freshBlock.getLastHash();
    freshBlock.setLastHash("A" + lastHash.substring(1, lastHash.length()));
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setLastHash(lastHash);
    assertEquals("verify that block with wrong last hash is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"last"));
  }
  
  @Test
  public void testValidate_withWrongCurrentBlockHash_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    String blockHash = freshBlock.getHash();
    freshBlock.setHash("A" + blockHash.substring(1, blockHash.length()));
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setHash(blockHash);
    assertEquals("verify that block with wrong hash is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"hash"));
  }
  
  @Test
  public void testValidate_withWrongMerkleRootHash_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    String merkleRoot = freshBlock.getTransactionMerkleRoot();
    freshBlock.setTransactionMerkleRoot("A" + merkleRoot.substring(1, merkleRoot.length()));
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setTransactionMerkleRoot(merkleRoot);
    assertEquals("verify that block with wrong merkleRoot is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"MerkleRoot"));
  }
  
  @Test
  public void testValidate_withFutureTimestamp_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    long timeStamp = freshBlock.getTimestamp();
    freshBlock.setTimestamp(new Date().getTime() + 10000);
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setTimestamp(timeStamp);
    assertEquals("verify that block with future timestamp is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"future"));
  }
  
  @Test
  public void testValidate_withWrongVersion_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    freshBlock.setVersion(2);
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setVersion(BlockMaker.CURRENT_BLOCK_VERSION);
    assertEquals("verify that block with wrong version is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"version"));
  }
  
  @Test
  public void testValidate_withTooHighTransactionSize_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() + 1);
    
    Assessment result = validator.validate(freshBlock);
    
    assertEquals("verify that block with too big transaction size is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"size"));
  }
  
  @Test
  public void testValidate_withFailedConsensusValidation_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    long realNonce = freshBlock.getConsent().getNonce();
    String oldHash = freshBlock.getHash();
    freshBlock.getConsent().setNonce(realNonce + 1);
    freshBlock.setHash(null);
    freshBlock.setHash(hasher.generateHash(freshBlock));
    when(blockchainStatistics.overallBlockHashes(any()))
        .thenReturn(ImmutableList.of(freshBlock.getHash()));
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.setHash(oldHash);
    freshBlock.getConsent().setNonce(realNonce);
    assertEquals("verify that block with consensus validation failed is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"difficulty target was not applied correctly"));
  }
  
  @Test
  public void testValidate_withFailedTransactionValidation_shouldValidateFail() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    BigDecimal oldAmount = freshBlock.getTransactions().get(0).getOutputs().get(0).getAmount();
    freshBlock.getTransactions().get(0).getOutputs().get(0).setAmount(new BigDecimal("1000"));
    
    Assessment result = validator.validate(freshBlock);
    
    freshBlock.getTransactions().get(0).getOutputs().get(0).setAmount(oldAmount);
    assertEquals("verify that block with transaction validation failed is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),"MerkleRoot-Hash of all transactions is wrong!"));
  }

  @Test
  public void testValidate_withMajorityIsCorrectHash_shouldValidateTrue() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    when(blockchainStatistics.overallBlockHashes(any()))
        .thenReturn(ImmutableList.of(freshBlock.getHash(), freshBlock.getHash(), freshBlock.getHash(),
            freshBlock.getHash() + "1", freshBlock.getHash() + "1"));

    Assessment result = validator.validate(freshBlock);

    assertEquals("verify that block with statistics validation failed is NOT valid", true, result.isValid());
  }

  @Test
  public void testValidate_withMajorityIsOtherHash_shouldValidateFalse() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    when(blockchainStatistics.overallBlockHashes(any()))
        .thenReturn(ImmutableList.of(freshBlock.getHash(),
            freshBlock.getHash() + "1", freshBlock.getHash() + "1"));

    Assessment result = validator.validate(freshBlock);

    assertEquals("verify that block with statistics validation failed is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),
        "Block hash is not used by the majority of nodes in the Blockchain!"));

  }

  @Test
  public void testValidate_withNoMajorityResults_shouldValidateFalse() {
    when(mockPressor.compressedByteSize(any())).thenReturn(Block.MAX_TRANSACTION_BYTE_SIZE.intValue() - 1);
    when(blockchainStatistics.overallBlockHashes(any())).thenReturn(new ArrayList<>());

    Assessment result = validator.validate(freshBlock);

    assertEquals("verify that block with statistics validation failed is NOT valid", false, result.isValid());
    assertTrue("verify that error message is correct", StringUtils.containsIgnoreCase(result.getReasonOfFailure(),
        "Block hash is not used by the majority of nodes in the Blockchain!"));
  }
}
