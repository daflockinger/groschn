package com.flockinger.groschn.messaging.distribution;


public interface SetEventListener<E> {
  
  void addedItem(E newItem);
  
  void removedItem(E removedItem);
  
  void updatedItem(E updatedItem);
}
