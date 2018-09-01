package com.flockinger.groschn.blockchain.validation.impl;

import static com.flockinger.groschn.blockchain.model.Block.MAX_AMOUNT_MINED_GROSCHN;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.BlockchainException;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
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
@Component("Transaction_Validator")
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
  @Autowired
  protected TransactionValidationHelper helper;
  
  @Override
  public Assessment validate(Transaction value) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new Transaction:
    try {
      //1. verify correct transactionHash
      verifyTransactionHash(value);
      //3. verify input sum is >= output sum
      verifyTransactionBalance(value);
      byte[] outputHash = hasher.generateListHash(value.getOutputs());
      
      for(int inCount=0; inCount < value.getInputs().size(); inCount++) {
        TransactionInput input = value.getInputs().get(inCount);
        //2. verify all input signed correctly all outputs
        verifySignature(input, outputHash);
        // verify basic statement values
        verifyBasicTransactionStatement(input, inCount);
        //7. input funds must be equal to the current balance for each input-publicKey
        isInputFundSufficient(input);
      }
      for(int outCount=0; outCount < value.getOutputs().size(); outCount++) {
        // verify output
        verifyBasicTransactionStatement(value.getOutputs().get(outCount), outCount);
      }
      isBlockValid.setValid(true);
    } catch (BlockchainException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }  
  return isBlockValid;
  }
  
  private <T extends TransactionOutput> void verifyBasicTransactionStatement(T statement, int inCount) {
    //4. sequence numbers must be starting from 1 and increasing only by 1 (no gaps)
    verifySequenceNumber(inCount + 1, statement.getSequenceNumber());
    //5. timestamps must not be in the future
    verifyTimestamp(statement.getTimestamp());
    //6. amount must be more than zero and less than MAX_COIN_AMOUNT
    verifyAmount(statement.getAmount());
  }
  
  private void verifyTransactionHash(Transaction transaction) {
    String transactionHash = transaction.getTransactionHash();
    transaction.setTransactionHash(null);
    boolean isHashCorrect = hasher.isHashCorrect(transactionHash, transaction);
    transaction.setTransactionHash(transactionHash);
    
    verifyAssessment(isHashCorrect, "Transaction hash is not correct!");
  }

  private void verifySignature(TransactionInput input, byte[] outputHash) {
    boolean isValid =  signer.isSignatureValid(outputHash, 
        input.getPublicKey(), input.getSignature());
    verifyAssessment(isValid, "Transaction signature is invalid for publicKey: " 
        + input.getPublicKey());
  }
  
  protected void verifyTransactionBalance(Transaction transaction) {
    verifyAssessment(helper.calcualteTransactionBalance(transaction) > 0, 
        "Total input amount must be higher than total output amount!");
  }
  
  private void verifySequenceNumber(long wantedNumber, Long presentNumber) {
    verifyAssessment(presentNumber != null && wantedNumber == presentNumber, 
        "Input/Output sequence is not correct! Must start with one and increment by one, without gaps!");
  }
  
  private void verifyTimestamp(Long timestamp) {
    Long now = new Date().getTime();
    verifyAssessment(timestamp != null && timestamp <= now, 
        "Transaction statements cannot be dated in the future!");
  }
  
  private void verifyAmount(BigDecimal amount) {
    boolean isAmountValid = amount.compareTo(BigDecimal.ZERO) > 0 
        && amount.compareTo(new BigDecimal(MAX_AMOUNT_MINED_GROSCHN)) < 0;
    verifyAssessment(isAmountValid, "Transaction statement amount must be"
        + "greater than zero and less than " + MAX_AMOUNT_MINED_GROSCHN + "!");
  }
    
  protected void isInputFundSufficient(TransactionInput input) {
    BigDecimal realBalance = wallet.calculateBalance(input.getPublicKey());
    verifyAssessment(realBalance.compareTo(input.getAmount()) == 0, 
        "Input amount must be exactly the same as the current balance!");
  }
  
  protected void verifyAssessment(boolean isValid, String errorMessage) {
    if(!isValid) {
      throw new AssessmentFailedException(errorMessage);
    }
  }
}
