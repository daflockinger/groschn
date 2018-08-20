package com.flockinger.groschn.blockchain.validation.impl;

import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;

@Component
public class TransactionValidator implements Validator<Transaction>{
  
  
  @Override
  public Assessment validate(Transaction value) {
    // TODO Auto-generated method stub
    return null;
  }

}
