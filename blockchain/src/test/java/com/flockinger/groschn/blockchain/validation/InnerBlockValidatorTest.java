package com.flockinger.groschn.blockchain.validation;

import static com.flockinger.groschn.blockchain.TestDataFactory.fifthBlock;
import static com.flockinger.groschn.blockchain.TestDataFactory.fourthBlock;
import static com.flockinger.groschn.blockchain.TestDataFactory.jsonMapper;
import static com.flockinger.groschn.blockchain.TestDataFactory.secondBlock;
import static com.flockinger.groschn.blockchain.TestDataFactory.thirdBlock;
import static org.junit.Assert.assertTrue;

import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockStorageServiceImpl;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.transaction.impl.BookkeeperImpl;
import com.flockinger.groschn.blockchain.validation.impl.BlockTransactionsValidator;
import com.flockinger.groschn.blockchain.validation.impl.BlockValidator;
import com.flockinger.groschn.blockchain.validation.impl.InnerBlockValidator;
import com.flockinger.groschn.blockchain.validation.impl.PowConsensusValidator;
import com.flockinger.groschn.blockchain.validation.impl.RewardTransactionValidator;
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidationHelper;
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidator;
import com.flockinger.groschn.blockchain.wallet.impl.WalletServiceImpl;
import com.flockinger.groschn.commons.compress.Compressor;
import com.flockinger.groschn.commons.hash.MerkleRootCalculator;
import com.flockinger.groschn.commons.hash.MultiHashGenerator;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = {InnerBlockValidator.class, BlockchainRepository.class, MultiHashGenerator.class,
        MerkleRootCalculator.class, Compressor.class, BlockStorageServiceImpl.class, PowConsensusValidator.class,
        BlockTransactionsValidator.class, TransactionValidator.class, RewardTransactionValidator.class, 
        TransactionValidationHelper.class, WalletServiceImpl.class, BookkeeperImpl.class})
public class InnerBlockValidatorTest extends BaseDbTest {

  @MockBean
  private TransactionManager managerMock;

  @MockBean(name = "blockValidator")
  private BlockValidator storageUsedValidatorMock;
  
  @Autowired
  private BlockchainRepository dao;

  @Autowired
  @Qualifier("innerBlockValidator")
  private InnerBlockValidator validator;
  @Autowired
  private ModelMapper mapper;

  
  @Before
  public void setup() throws Exception {
    dao.deleteAll();
    var modifiedGenesis = Block.GENESIS_BLOCK();
    modifiedGenesis.setHash("2c7a509afb7c6675774b75e999e8191c7790161da9843f16b7519cb756200e3cb6d7ea6b8d4078c4805d1205b415b9d83e5d5b0a10e16d9f70e8d1deef47a15e");
    modifiedGenesis.setTimestamp(484696800000L);
    modifiedGenesis.getConsent().setMilliSecondsSpentMining(439L);
    modifiedGenesis.getConsent().setTimestamp(1536407028239L);
    modifiedGenesis.getConsent().setNonce(69532L);
    dao.insert(ImmutableList.of(mapper.map(modifiedGenesis, StoredBlock.class),
        jsonMapper.readValue(secondBlock, StoredBlock.class),
        jsonMapper.readValue(thirdBlock, StoredBlock.class)));
  }

  @Test
  public void testValidate_withFreshGoodBlock_shouldValidateTrue() throws Exception {
    Block freshBlock = mapper.map(jsonMapper.readValue(fourthBlock, StoredBlock.class), Block.class);
    
    var result = validator.validate(freshBlock);
    
    assertTrue("fresh valid block should be validated correctly", result.isValid());
  }
  
  
  @Test
  public void testValidate_withRevalidatingCurrentBlock_shouldValidateTrue() throws Exception {
    Block freshBlock = mapper.map(jsonMapper.readValue(thirdBlock, StoredBlock.class), Block.class);
    
    var result = validator.validate(freshBlock);
    
    assertTrue("current valid block should be validated correctly", result.isValid());
  }
  
  @Test
  public void testValidate_withRevalidatingLastBlockAndMoreBlocks_shouldValidateTrue() throws Exception {
    dao.insert(ImmutableList.of(jsonMapper.readValue(fourthBlock, StoredBlock.class),
        jsonMapper.readValue(fifthBlock, StoredBlock.class)));
    
    Block freshBlock = mapper.map(jsonMapper.readValue(fourthBlock, StoredBlock.class), Block.class);
    
    var result = validator.validate(freshBlock);
    
    assertTrue("current valid block should be validated correctly", result.isValid());
  }
  
  @Test
  public void testValidate_withRevalidatingOlderBlock_shouldValidateTrue() throws Exception {
    Block freshBlock = mapper.map(jsonMapper.readValue(secondBlock, StoredBlock.class), Block.class);
    
    var result = validator.validate(freshBlock);
    
    assertTrue("old valid block should be validated correctly", result.isValid());
  }
}
