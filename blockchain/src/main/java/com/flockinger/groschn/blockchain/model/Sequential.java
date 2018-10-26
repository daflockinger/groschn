package com.flockinger.groschn.blockchain.model;

public interface Sequential extends Comparable<Sequential>{
  Long getSequenceNumber();
  
  default int compareTo(Sequential o) {
    if(o == null) {
      return 1;
    }
    if(this.getSequenceNumber() == null && o.getSequenceNumber() == null) {
      return 0;
    } else if (this.getSequenceNumber() == null) {
      return -1;
    } else if (o.getSequenceNumber() == null) {
      return 1;
    }
    return this.getSequenceNumber().compareTo(o.getSequenceNumber());
  }
}
