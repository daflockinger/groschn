package com.flockinger.groschn.blockchain.validation;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.blockworks.impl.BlockStorageServiceImpl;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.transaction.TransactionManager;
import com.flockinger.groschn.blockchain.transaction.impl.BookkeeperImpl;
import com.flockinger.groschn.blockchain.validation.impl.BlockTransactionsValidator;
import com.flockinger.groschn.blockchain.validation.impl.InnerBlockValidator;
import com.flockinger.groschn.blockchain.validation.impl.PowConsensusValidator;
import com.flockinger.groschn.blockchain.validation.impl.RewardTransactionValidator;
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidationHelper;
import com.flockinger.groschn.blockchain.validation.impl.TransactionValidator;
import com.flockinger.groschn.blockchain.wallet.impl.WalletServiceImpl;
import com.flockinger.groschn.commons.MerkleRootCalculator;
import com.flockinger.groschn.commons.compress.CompressionUtils;
import com.flockinger.groschn.commons.hash.MultiHashGenerator;
import com.google.common.collect.ImmutableList;

@RunWith(SpringRunner.class)
@ContextConfiguration(
    classes = {InnerBlockValidator.class, BlockchainRepository.class, MultiHashGenerator.class,
        MerkleRootCalculator.class, CompressionUtils.class, BlockStorageServiceImpl.class, PowConsensusValidator.class, 
        BlockTransactionsValidator.class, TransactionValidator.class, RewardTransactionValidator.class, 
        TransactionValidationHelper.class, WalletServiceImpl.class, BookkeeperImpl.class})
public class InnerBlockValidatorTest extends BaseDbTest {

  @MockBean
  private TransactionManager managerMock;
  
  @Autowired
  private BlockchainRepository dao;
  @Autowired
  private InnerBlockValidator validator;
  @Autowired
  private ModelMapper mapper;

  private final static ObjectMapper jsonMapper = new ObjectMapper();
  private final String secondBlock = "{\"position\" : 2,\"hash\" : \"00000bb088b358992c70dfa85c1417e85f936be0154956dd6b6961f471e97d9d22ab4f5572ebfb59a5e3fbe4a69c2f4b9413b2fd982a92905bddd3c66517673b\",\"lastHash\" : \"2c7a509afb7c6675774b75e999e8191c7790161da9843f16b7519cb756200e3cb6d7ea6b8d4078c4805d1205b415b9d83e5d5b0a10e16d9f70e8d1deef47a15e\",\"timestamp\" : 1541942337574,\"transactionMerkleRoot\" : \"89bc7f7e053d33edb8902e8ffe80d7c97973814b4512d8ba1dad553c18a7c4c3b0505eafc1a93ac6e76bb2ad13df93a9dbc292b96ca20b33c892892105f42c2e\",\"version\" : 1,\"transactions\" : [{\"inputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942336552,\"sequenceNumber\" : 1,\"signature\" : \"AN1rKvtYhASrS1U5miWKQjJNFizpM6hPEsyJkHwzspySFHTwGygKtzYZcarqZhtNNeQm1c3eUrNqga8FBep2rnzk4VKuKC9P4\"}],\"outputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942336552,\"sequenceNumber\" : 1}],\"transactionHash\" : \"5cd946330d6266bc21034038a0e9c5204b606c61892eea923a0d9ec921deb73359a0c872c55b5546d99a327cb9098f0d7d9b1c64afc7d2b067d0d833d90791c8\"}],\"consent\" : {\"nonce\" : 879070,\"timestamp\" : 1541942337582,\"difficulty\" : 5,\"milliSecondsSpentMining\" : 9906,\"type\" : \"PROOF_OF_WORK\"}}";
  private final String thirdBlock = "{\"position\" : 3,\"hash\" : \"000000d8e11bbfb09143f588f1708e86316ee40610ff5bb68c312c85e5968614d310edf8e0f1d5161284d80ccfddbb47096837d9fa97d586ae50d3373a6a5261\",\"lastHash\" : \"00000bb088b358992c70dfa85c1417e85f936be0154956dd6b6961f471e97d9d22ab4f5572ebfb59a5e3fbe4a69c2f4b9413b2fd982a92905bddd3c66517673b\",\"timestamp\" : 1541942349518,\"transactionMerkleRoot\" : \"dd2cbf8e4a98e0b53b299c90e464f2d8d15ed0c24ca392b046ba90394dd104f16f64edde617603c4454cbd2ab51d708bf9314464ef87e9f119946b6dff8e1b43\",\"version\" : 1,\"transactions\" : [{\"inputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCxvknBRgXLZLkWDXhC9gDhW1yWLbFjTbWhsTnF6QmyvJfBVnCC1a1SxmpZVJn4sSg9ufZyyyA7r8sAwwe5Nn1Sv1h\",\"timestamp\" : \"1541942349503\",\"sequenceNumber\" : 1,\"signature\" : \"AN1rKvtonYs8pzfdttv6LXTUaybCgyYyBvTwmKMH9t2Dq5UaQ8Yxd2o93XRPFi1UNqkvArWK4RkKsYD3LYBtHtQYc5tK5p5od\"}],\"outputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCxvknBRgXLZLkWDXhC9gDhW1yWLbFjTbWhsTnF6QmyvJfBVnCC1a1SxmpZVJn4sSg9ufZyyyA7r8sAwwe5Nn1Sv1h\",\"timestamp\" : 1541942349503,\"sequenceNumber\" : 1}],\"transactionHash\" : \"3f1a7902faadf35413bc5015ed5c78a8af2d68cec84ef0aac2f8a7617a8aea5a1c78182b1f02c9d9039fce869acd46e00282c49b2cfe1142a88c30d7cf8b4849\"}],\"consent\" : {\"nonce\" : 2004544,\"timestamp\" : 1541942349518,\"difficulty\" : 6,\"milliSecondsSpentMining\" : 37065,\"type\" : \"PROOF_OF_WORK\"}}";
  private final String fourthBlock = "{\"position\" : 4,\"hash\" : \"0000001c89487b269afe463edfeb8e242fbdb89d072f44e703fb7074ebd5a1d5bb0a51dc5fb78ded9217cb44410462542c1ab65450e07a86ececab2d7cd7e9d8\",\"lastHash\" : \"000000d8e11bbfb09143f588f1708e86316ee40610ff5bb68c312c85e5968614d310edf8e0f1d5161284d80ccfddbb47096837d9fa97d586ae50d3373a6a5261\",\"timestamp\" : 1541942386765,\"transactionMerkleRoot\" : \"49b9248dfd15f6b2f1369b5c8767525ecef64300ec048010f31b24ec84c13d7d3d48f0fdebd7a313f32b96664bf8e7689b3b51a921c4528e6773dc7d14cd7910\",\"version\" : 1,\"transactions\" : [{\"inputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942386691,\"sequenceNumber\" : 1,\"signature\" : \"AN1rKvtk6JWP9WHh5YauSoRaZwK4YQ9KvG8F6xwkB76S1bTDbCwVNVSezRtiGr5DzSCbWaGK1rKd8LAn8roVCZDrCcUo4Swek\"},{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942386708,\"sequenceNumber\" : 2,\"signature\" : \"AN1rKvt51sCS5K1sm8bHeGdf8LEZfbPQ8J4es9d1zrV4wXaiYBsyhQqayRgvM4E5ZoAYaoFYCRkkgknwbvgNXoYye2oXQo7ex\"}],\"outputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942386691,\"sequenceNumber\" : 1},{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942386708,\"sequenceNumber\" : 2}],\"transactionHash\" : \"4d04d52bdd7f4efba3e8f7c1c780b36bfcf358c7ca904434fd7d7f94316b59ab8205b889a596615b9bad53b397c0be9aad5a517ca0a92a59b81d0daeb1c5a57e\"}],\"consent\" : {\"nonce\" : 475450,\"timestamp\" : 1541942386765,\"difficulty\" : 5,\"milliSecondsSpentMining\" : 14188,\"type\" : \"PROOF_OF_WORK\"}}";
  private final String fifthBlock = "{ \"position\" : 5,\"hash\" : \"0000002f7a23901ddf2396580ec43d296a21cca7e119d726e6dcdb3999eb4095570bb27e121c7c6b120ddded076de6e42312bf25df4dda016ccabbf19cc86835\",\"lastHash\" : \"0000001c89487b269afe463edfeb8e242fbdb89d072f44e703fb7074ebd5a1d5bb0a51dc5fb78ded9217cb44410462542c1ab65450e07a86ececab2d7cd7e9d8\",\"timestamp\" : 1541942416722,\"transactionMerkleRoot\" : \"2cfe1f9b73ee51c180edc07e4cb5377db9edb2b91589b16986ea6e2e2aa4096b669d8b11991757794f7f2d34445de28b87986186bb0a0eaac51569c5d58740b9\",\"version\" : 1,\"transactions\" : [{\"inputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942416643,\"sequenceNumber\" : 1,\"signature\" : \"AN1rKoWP3QRJF9faviFyY8TMphyWYEW2GZjpkqSoeu3wJidZBnGL1MuUXnx9V3yo2RzzguHQzSYxrX5Wz9RjQQy2obKYDr6Vo\"},{\"amount\" : \"200.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942416655,\"sequenceNumber\" : 2,\"signature\" : \"381yXZPZbAgJHebxW1FXww9TnDMoBiyLUj3XBcJ7yV2uDZioU3vCfH1QETLe2Rp71hS1wbe1RVQsQmL9KoJZfTxuDDmrWw6q\"}],\"outputs\" : [{\"amount\" : \"100.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942416643,\"sequenceNumber\" : 1},{\"amount\" : \"200.0\",\"publicKey\" : \"PZ8Tyr4Nx8MHsRAGMpZmZ6TWY63dXWSCyHgmYGQ7vQthECFNuruEVLAhxBHEgrDhpMmQ1kAWFdxJKVvEL5vbP6kkv4puwqB6XWd8zLuhwiUGRX7Qxojj8tKx\",\"timestamp\" : 1541942416655,\"sequenceNumber\" : 2}],\"transactionHash\" : \"fb7a19fa36e89fab5786b31fd7ccc75d5959b00b89caaa4a2a7b97331b204308bf32da8ff91c57eba5feff60a8d055fec65c76ab3329c73a0f224ee91404732d\"}],\"consent\" : {\"nonce\" : 3280883,\"timestamp\" : 1541942416722,\"difficulty\" : 6,\"milliSecondsSpentMining\" : 105357,\"type\" : \"PROOF_OF_WORK\"}}";
  
  
  @Before
  public void setup() throws Exception {
    dao.deleteAll();
    dao.insert(ImmutableList.of(mapper.map(Block.GENESIS_BLOCK(), StoredBlock.class), 
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
    System.out.println(result.getReasonOfFailure());
  }
  
  @Test
  public void testValidate_withRevalidatingLastBlockAndMoreBlocks_shouldValidateTrue() throws Exception {
    dao.insert(ImmutableList.of(mapper.map(Block.GENESIS_BLOCK(), StoredBlock.class), 
        jsonMapper.readValue(fourthBlock, StoredBlock.class), 
        jsonMapper.readValue(fifthBlock, StoredBlock.class)));
    
    Block freshBlock = mapper.map(jsonMapper.readValue(fourthBlock, StoredBlock.class), Block.class);
    
    var result = validator.validate(freshBlock);
    
    assertTrue("current valid block should be validated correctly", result.isValid());
    System.out.println(result.getReasonOfFailure());
  }
  
  @Test
  public void testValidate_withRevalidatingOlderBlock_shouldValidateTrue() throws Exception {
    Block freshBlock = mapper.map(jsonMapper.readValue(secondBlock, StoredBlock.class), Block.class);
    
    var result = validator.validate(freshBlock);
    
    assertTrue("old valid block should be validated correctly", result.isValid());
    System.out.println(result.getReasonOfFailure());
  }
}
