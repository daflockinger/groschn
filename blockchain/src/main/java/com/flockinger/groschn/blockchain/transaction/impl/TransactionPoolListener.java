package com.flockinger.groschn.blockchain.transaction.impl;

import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.transaction.TransactionDto;
import com.flockinger.groschn.messaging.distribution.SetEventListener;

@Component
public class TransactionPoolListener implements SetEventListener<TransactionDto> {

  @Override
  public void addedItem(TransactionDto newItem) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removedItem(TransactionDto removedItem) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updatedItem(TransactionDto updatedItem) {
    // TODO Auto-generated method stub
    
  }

  

}
