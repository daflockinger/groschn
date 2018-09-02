package com.flockinger.groschn.blockchain.validation.impl;

import static com.flockinger.groschn.blockchain.model.Block.MAX_AMOUNT_MINED_GROSCHN;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.model.Sequential;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.model.TransactionOutput;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;
import com.flockinger.groschn.blockchain.wallet.WalletService;

/**
 * Single Transaction Validator should be used for new incomming <br>
 * independent Transactions to verify.
 */
public class TransactionValidator implements Validator<Transaction> {
  /*
   Transaction check (regarding the Transaction itself):
   *****************************************************
   1. verify correct transactionHash
   2. verify all input signed correctly all outputs
   3. verify input sum is > output sum
   Input/Output only checks: 
   ********************
   4. sequence numbers must be correct:
     - starting from 1
     - increasing only by 1 (no gaps)
   5. timestamps must not be in the future
   - publicKey must be a valid signature-key
   6. amount must be:
     - more than zero
     - less than MAX_COIN_AMOUNT
     - TODO figure out minimum currency value (e.g. how many numbers behind the comma is still fine!) 
  Input only checks:
  ******************
  7. verify that the input funds are correct
    - has the public-key owner enough funds?
      - make the fund check as fast as possible (since probably many are requested)
    - see if the PointCut is any help in this calculation?
   **/
  @Autowired
  private HashGenerator hasher;
  @Autowired
  @Qualifier("ECDSA_Signer")
  private Signer signer;
  @Autowired
  private WalletService wallet;
  
  @Override
  public Assessment validate(Transaction value) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new Transaction:
    try {
      //1. verify correct transactionHash
      verifyTransactionHash(value);
      //2. verify all input signed correctly all outputs
      verifySignatures(value);
      //3. verify input sum is >= output sum
      verifyPositiveTransactionBalance(value);
      //4. sequence numbers must be starting from 1 and increasing only by 1 (no gaps)
      verifySequenceNumbers(value);
      //5. timestamps must not be in the future
      verifyTimestamps(value);
      //6. amount must be more than zero and less than MAX_COIN_AMOUNT
      verifyAmounts(value);
      //7. input funds must be equal to the current balance for each input-publicKey
      verifyInputFundsAreSufficient(value);
      
      isBlockValid.setValid(true);
    } catch (BlockchainException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }  
  return isBlockValid;
  }
  
  private void verifyTransactionHash(Transaction transaction) {
    String transactionHash = transaction.getTransactionHash();
    transaction.setTransactionHash(null);
    boolean isHashCorrect = hasher.isHashCorrect(transactionHash, transaction);
    transaction.setTransactionHash(transactionHash);
    
    verifyAssessment(isHashCorrect, "Transaction hash is not correct!");
  }
  
  private void verifySignatures(Transaction transaction) {
    byte[] outputHash = hasher.generateListHash(transaction.getOutputs());
    
    for(TransactionInput input: transaction.getInputs()) {
      boolean isValid =  signer.isSignatureValid(outputHash, 
          input.getPublicKey(), input.getSignature());
      verifyAssessment(isValid, "Transaction signature is invalid for publicKey: " 
          + input.getPublicKey());
    }
  }
  
  private void verifyPositiveTransactionBalance(Transaction transaction) {
    var inputAmount = transaction.getInputs().stream().map(TransactionInput::getAmount)
      .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    var outputAmount = transaction.getOutputs().stream().map(TransactionOutput::getAmount)
        .filter(Objects::nonNull).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
    verifyAssessment(inputAmount.compareTo(outputAmount) > 0, 
        "Total input amount must be higher than total output amount!");
  }
  
  private void verifySequenceNumbers(Transaction transaction) {
    boolean areSequencesCorrect = isSequenceCorrect(transaction.getInputs()) && isSequenceCorrect(transaction.getOutputs());
    verifyAssessment(areSequencesCorrect, "Input/Output sequence is not correct! Must start with one and increment by one, without gaps!");
  }
  
  private <T extends Sequential> boolean isSequenceCorrect(List<T> transactionStatements) {
    var longSequence = transactionStatements.stream()
        .map(Sequential::getSequenceNumber).sorted().collect(Collectors.toList());
    var wantedSequence = LongStream.range(1, longSequence.stream().reduce(Long::max).orElse(0l) + 1)
        .boxed().collect(Collectors.toList());
    return CollectionUtils.isEqualCollection(longSequence, wantedSequence);
  }
  
  private void verifyTimestamps(Transaction transaction) {
    transaction.getOutputs().stream().map(TransactionOutput::getTimestamp)
      .forEach(this::verifyTimestamp);
    transaction.getInputs().stream().map(TransactionInput::getTimestamp)
      .forEach(this::verifyTimestamp);
  }
  
  private void verifyTimestamp(Long timestamp) {
    Long now = new Date().getTime();
    verifyAssessment(timestamp != null && timestamp <= now, 
        "Transaction statements cannot be dated in the future!");
  }
  
  private void verifyAmounts(Transaction transaction) {
    boolean areOutputAmountsValid = transaction.getOutputs().stream().map(TransactionOutput::getAmount)
        .filter(Objects::nonNull).allMatch(this::isAmountValid);
    boolean areInputAmountsValid = transaction.getInputs().stream().map(TransactionInput::getAmount)
        .filter(Objects::nonNull).allMatch(this::isAmountValid);
    verifyAssessment(areInputAmountsValid && areOutputAmountsValid, "Transaction statement amount must be"
        + "greater than zero and less than " + MAX_AMOUNT_MINED_GROSCHN + "!");
  }
  
  private boolean isAmountValid(BigDecimal amount) {
    return amount.compareTo(BigDecimal.ZERO) > 0 
        && amount.compareTo(new BigDecimal(MAX_AMOUNT_MINED_GROSCHN)) < 0;
  }
  
  private void verifyInputFundsAreSufficient(Transaction transaction) {
    for(TransactionInput input: transaction.getInputs()) {
      isInputFundSufficient(input);
    }
  }
  
  private void isInputFundSufficient(TransactionInput input) {
    BigDecimal realBalance = wallet.calculateBalance(input.getPublicKey());
    verifyAssessment(realBalance.compareTo(input.getAmount()) == 0, 
        "Input amount must be exactly the same as the current balance!");
  }
  
  private void verifyAssessment(boolean isValid, String errorMessage) {
    if(!isValid) {
      throw new AssessmentFailedException(errorMessage);
    }
  }
}
