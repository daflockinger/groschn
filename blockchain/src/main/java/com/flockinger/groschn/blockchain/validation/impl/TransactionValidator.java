package com.flockinger.groschn.blockchain.validation.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.validation.AssessmentFailedException;
import com.flockinger.groschn.blockchain.exception.validation.ValidationException;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.model.TransactionInput;
import com.flockinger.groschn.blockchain.util.sign.Signer;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;

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
   3. verify input sum is >= output sum (can/should be higher cause of Change) TODO maybe must be higher!
   
   Input/Output only checks: 
   ********************
   - sequence numbers must be correct:
     - starting from 1
     - increasing only by 1 (no gaps)
   - timestamps must not be in the future
   - publicKey must be a valid signature-key
   - amount must be:
     - more than zero
     - less than MAX_COIN_AMOUNT
     - TODO figure out minimum currency value (e.g. how many numbers behind the comma is still fine!)
     
  Input only checks:
  ******************
  - verify that the input funds are correct
    - has the public-key owner enough funds?
      - make the fund check as fast as possible (since probably many are requested)
    - see if the PointCut is any help in this calculation?
    
   private String transactionHash;
 
   INPUT:
     private String signature;
     private TransactionPointCut previousOutputTransaction;
     private BigDecimal amount;
     private String publicKey;
     private Long timestamp;
     private Long sequenceNumber;
   
   OUTPUT:
     private BigDecimal amount;
     private String publicKey;
     private Long timestamp;
     private Long sequenceNumber;
   * */
  
  @Autowired
  private HashGenerator hasher;
  @Autowired
  @Qualifier("ECDSA_Signer")
  private Signer signer;
  
  @Override
  public Assessment validate(Transaction value) {
    Assessment isBlockValid = new Assessment();
    // Validations for a new Transaction:
    try {
      //1. verify correct transactionHash
      verifyTransactionHash(value);
      //2. verify all input signed correctly all outputs
      verifySignatures(value);
      
      
      isBlockValid.setValid(true);
    } catch (ValidationException e) {
      isBlockValid.setValid(false);
      isBlockValid.setReasonOfFailure(e.getMessage());
    }   
  return isBlockValid;
  }
  
  void verifyTransactionHash(Transaction transaction) {
    String transactionHash = transaction.getTransactionHash();
    transaction.setTransactionHash(null);
    boolean isHashCorrect = hasher.isHashCorrect(transactionHash, transaction);
    transaction.setTransactionHash(transactionHash);
    
    verifyAssessment(isHashCorrect, "Transaction hash is not correct!");
  }
  
  //TODO continue here!!
  void verifySignatures(Transaction transaction) {
   // byte[] outputHash = Hex.
    
    for(TransactionInput input: transaction.getInputs()) {
     // signer.isSignatureValid(transactionHash, publicKey, signature)
    }
    
  }
  
  
  private void verifyAssessment(boolean isValid, String errorMessage) {
    if(!isValid) {
      throw new AssessmentFailedException(errorMessage);
    }
  }
}
