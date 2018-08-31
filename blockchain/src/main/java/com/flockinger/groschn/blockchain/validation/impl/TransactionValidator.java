package com.flockinger.groschn.blockchain.validation.impl;

import java.util.List;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.InputKeyAware;
import com.flockinger.groschn.blockchain.validation.Validator;

public class TransactionValidator implements Validator<Transaction>, InputKeyAware {

  @Override
  public Assessment validate(Transaction value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addUsedInputPublicKeys(List<String> usedPublicKeys) {
    // TODO Auto-generated method stub
    
  }

}
