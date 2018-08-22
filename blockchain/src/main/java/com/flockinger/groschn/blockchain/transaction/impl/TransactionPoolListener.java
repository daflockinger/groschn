package com.flockinger.groschn.blockchain.transaction.impl;

import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.model.Transaction;
import com.flockinger.groschn.messaging.distribution.SetEventListener;

@Component
public class TransactionPoolListener implements SetEventListener<Transaction> {

  @Override
  public void addedItem(Transaction newItem) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removedItem(Transaction removedItem) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updatedItem(Transaction updatedItem) {
    // TODO Auto-generated method stub
    
  }

  

}
