package com.flockinger.groschn.blockchain.validation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.blockworks.BlockStorageService;
import static com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm.MINING_RATE_MILLISECONDS;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.validation.impl.PowConsensusValidator;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PowConsensusValidator.class})
public class PowConsensusValidatorTest {

  @Autowired
  private PowConsensusValidator validator;

  @MockBean
  private BlockStorageService mockService;


  @Test
  public void testValidate_withValidBlock_shouldReturnValid() {
    Block block = createValidBlock();
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(createValidBlock());

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify valid block asserted true", true, result.isValid());
  }

  @Test
  public void testValidate_withNotEnoughLeadingZeros_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.setHash(
        "000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(createValidBlock());
    
    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block having not enough leading zeros asserted false", false,
        result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "applied"));
  }
  
  @Test
  public void testValidate_withOnesInsteadOfZerosLeading_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.setHash(
        "1111cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(createValidBlock());
    
    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block having leading ones instead of zeros asserted false", false,
        result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "applied"));
  }

  @Test
  public void testValidate_withTooManyLeadingZeros_shouldStillBeFine() {
    Block block = createValidBlock();
    block.setHash(
        "00000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(createValidBlock());
    
    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block with too many leading zeros asserted true", true, result.isValid());
  }

  @Test
  public void testValidate_withNegativeDifficulty_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.getConsent().setDifficulty(-1);
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(createValidBlock());
    
    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block having negative difficulty asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "negative"));
  }
  
  @Test
  public void testValidate_withZeroDifficulty_shouldStillWork() {
    Block block = createValidBlock();
    block.getConsent().setDifficulty(0);
    block.setHash("1b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75");
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(block);
    
    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block having zero difficulty asserted true", true, result.isValid());
  }

  @Test
  public void testValidate_withSlowMiningButNoDifficultyDecrease_shouldReturnInvalid() {
    Block block = createValidBlock();
    Block lastBlock = createValidBlock();
    lastBlock.getConsent().setMilliSecondsSpentMining(MINING_RATE_MILLISECONDS + 1);
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(lastBlock);

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block with wrong difficulty asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "slow"));
  }

  @Test
  public void testValidate_withTooFastMiningButNoDifficultyIncrease_shouldReturnInvalid() {
    Block block = createValidBlock();
    Block lastBlock = createValidBlock();
    lastBlock.getConsent().setMilliSecondsSpentMining(MINING_RATE_MILLISECONDS - 1);
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(lastBlock);

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block with wrong difficulty asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "fast"));
  }
  
  @Test
  public void testValidate_withExactMiningButDifficultyIncrease_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.getConsent().setDifficulty(5);
    block.setHash("00000b9f909cd56bc01c24b5ceefea2405a4d118fa18a");
    Block lastBlock = createValidBlock();
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(lastBlock);

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block with wrong difficulty asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "exact"));
  }
  
  @Test
  public void testValidate_withExactMiningButDifficultyDecrease_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.getConsent().setDifficulty(3);
    block.setHash("000b9f909cd56bc01c24b5ceefea2405a4d118fa18a");
    Block lastBlock = createValidBlock();
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(lastBlock);

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block with wrong difficulty asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "exact"));
  }

  @Test
  public void testValidate_withDifficultyIncreasedWayTooMuch_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.getConsent().setDifficulty(10);
    block.setHash("00000000009f909cd56bc01c24b5ceefea2405a4d118fa18a");
    Block lastBlock = createValidBlock();
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(lastBlock);

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block with wrong difficulty asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "increase/decrease"));
  }

  @Test
  public void testValidate_withDifficultyDecreasedWayTooMuch_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.getConsent().setDifficulty(1);
    block.setHash("09f909cd56bc01c24b5ceefea2405a4d118fa18a");
    Block lastBlock = createValidBlock();
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(lastBlock);

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block with wrong difficulty asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "increase/decrease"));
  }

  @Test
  public void testValidate_withFutureTimestamp_shouldReturnInvalid() {
    Block block = createValidBlock();
    block.getConsent().setTimestamp(new Date().getTime() + 50000l);
    when(mockService.getLatestProofOfWorkBlock()).thenReturn(createValidBlock());

    Assessment result = validator.validate(block);

    assertNotNull("verify result is not null", result);
    assertEquals("verify block having future timestamp asserted false", false, result.isValid());
    assertTrue("verify error message is correct",
        StringUtils.containsIgnoreCase(result.getReasonOfFailure(), "future"));
  }


  private Block createValidBlock() {
    Block block = new Block();
    block.setHash(
        "0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    Consent consent = new Consent();
    consent.setDifficulty(4);
    consent.setMilliSecondsSpentMining(MINING_RATE_MILLISECONDS);
    consent.setNonce(123l);
    consent.setTimestamp(2000l);
    consent.setType(ConsensusType.PROOF_OF_WORK);
    block.setConsent(consent);
    return block;
  }

}