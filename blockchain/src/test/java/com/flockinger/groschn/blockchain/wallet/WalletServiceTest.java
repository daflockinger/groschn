package com.flockinger.groschn.blockchain.wallet;

import static com.flockinger.groschn.blockchain.TestDataFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import com.flockinger.groschn.blockchain.BaseDbTest;
import com.flockinger.groschn.blockchain.repository.BlockchainRepository;
import com.flockinger.groschn.blockchain.repository.WalletRepository;
import com.flockinger.groschn.blockchain.repository.model.StoredBlock;
import com.flockinger.groschn.blockchain.repository.model.StoredTransaction;
import com.flockinger.groschn.blockchain.repository.model.StoredTransactionOutput;
import com.flockinger.groschn.blockchain.util.crypto.impl.KeyAESCipher;
import com.flockinger.groschn.blockchain.wallet.impl.WalletServiceImpl;

@ContextConfiguration(classes = {WalletServiceImpl.class, BlockchainRepository.class, WalletRepository.class, KeyAESCipher.class})
public class WalletServiceTest extends BaseDbTest {

  @Autowired
  private BlockchainRepository blockDao;
  @Autowired
  private WalletService service;

  @Before
  public void setup() {
    blockDao.deleteAll();
  }
  
  // TODO check if more tests would be needed for this!
  @Test
  public void testCalculateBalance_withMultipleInputsAndOutputs_shouldCalculateCorrectly() {
    blockDao.saveAll(createRandomBlocksWith(false, true, true, "daRealMaster"));
    
    BigDecimal balance = service.calculateBalance("daRealMaster");
    
    assertNotNull("verify calculated balance is not null", balance);
    assertEquals("verify balance is correct", 180, balance.intValue());
  }

  @Test
  public void testCalculateBalance_withMultipleInputsAndOutputsAndMultipleOutputsInOneTransaction_shouldCalculateCorrectly() {
    blockDao.saveAll(createRandomBlocksWith(true, true, false, "daRealMaster"));
    
    BigDecimal balance = service.calculateBalance("daRealMaster");
    
    assertNotNull("verify calculated balance is not null", balance);
    assertEquals("verify balance is correct", 360, balance.intValue());
  }

  @Test
  public void testCalculateBalance_withOnlySomeOutputs_shouldCalculateCorrectly() {
    blockDao.saveAll(createRandomBlocksWith(false, false, true, "daRealMaster"));
    
    BigDecimal balance = service.calculateBalance("daRealMaster");
    
    assertNotNull("verify calculated balance is not null", balance);
    assertEquals("verify balance is correct", 2200, balance.intValue());
  }

  @Test
  public void testCalculateBalance_withOnlySomeOutputsAndMultipleOutputsInOneTransaction_shouldCalculateCorrectly() {
    blockDao.saveAll(createRandomBlocksWith(true, false, false, "daRealMaster"));
    
    BigDecimal balance = service.calculateBalance("daRealMaster");
    
    assertNotNull("verify calculated balance is not null", balance);
    assertEquals("verify balance is correct", 4400, balance.intValue());
  }
  

  @Test
  public void testCalculateBalance_withNothing_shouldCalculateZero() {
    blockDao.saveAll(createTotallyRandomBlocks());
    
    BigDecimal balance = service.calculateBalance("someKey123");
    
    assertNotNull("verify calculated balance is not null", balance);
    assertEquals("verify balance is correct", 0, balance.intValue());
  }


  public List<StoredBlock> createTotallyRandomBlocks() {
    var blocks = new ArrayList<StoredBlock>();
    IntStream.range(0, 20).forEach(count -> {
      StoredBlock block = new StoredBlock();
      block.setPosition(Long.valueOf(count));
      var transactions = new ArrayList<StoredTransaction>();
      IntStream.range(0, 10).forEach(tCount -> {
        transactions.add(createRandomTransactionWith(null, null, null));
      });
      block.setTransactions(transactions);
      blocks.add(block);
    });
    return blocks;
  }


  public List<StoredBlock> createRandomBlocksWith(boolean multipleOut, boolean singleIn,
      boolean singleOut, String masterPubKey) {
    var blocks = createTotallyRandomBlocks();
    if (multipleOut || singleOut) {
      IntStream.range(0, blocks.size()).forEach(blockCount -> {
        int multiplier = (20 - blockCount);
        if (blockCount % 2 == 0 && (multipleOut || singleOut)) { 
          blocks.get(blockCount).getTransactions()
            .get(3).setOutputs(createTransactionOutputs(multipleOut, masterPubKey,multiplier));
          blocks.get(blockCount).getTransactions()
            .get(7).setOutputs(createTransactionOutputs(multipleOut, masterPubKey,multiplier));
        }
        if(blockCount % 7 == 0 && singleIn) {
          var modifyInput1 = blocks.get(blockCount).getTransactions().get(3)
              .getInputs().get(RandomUtils.nextInt(0, 4));
          blocks.get(blockCount).getTransactions().get(3).getOutputs().get(1).setAmount(new BigDecimal(123));
          modifyInput1.setPublicKey(masterPubKey);
          modifyInput1.setTimestamp(1001l);
          
          var modifyInput2 = blocks.get(blockCount).getTransactions().get(7)
              .getInputs().get(RandomUtils.nextInt(0, 4));
          modifyInput2.setPublicKey(masterPubKey);
          modifyInput2.setTimestamp(1002l);
        }
      });
    }
    return blocks;
  }
  
  private List<StoredTransactionOutput> createTransactionOutputs(boolean shouldMultipleOutputsBeModified, String masterPubKey, int multiplier) {
    var outputs = new ArrayList<StoredTransactionOutput>();
    outputs.add(createRandomTransactionOutputWith(1, null));
    outputs.add(createRandomTransactionOutputWith(2, masterPubKey, multiplier * 10l));
    outputs.add(createRandomTransactionOutputWith(3, null));
    outputs.add(createRandomTransactionOutputWith(4, null));
    outputs.add(createRandomTransactionOutputWith(5, masterPubKey, shouldMultipleOutputsBeModified ? (multiplier * 7l) : 0l));
    outputs.add(createRandomTransactionOutputWith(6, null));
    outputs.add(createRandomTransactionOutputWith(7, null));
    outputs.add(createRandomTransactionOutputWith(8, null));
    outputs.add(createRandomTransactionOutputWith(9, masterPubKey, shouldMultipleOutputsBeModified ? (multiplier * 3l) : 0l));
    return outputs;
  }
}
