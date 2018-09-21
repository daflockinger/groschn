package com.flockinger.groschn.blockchain.model;

import java.io.Serializable;

public interface Hashable<T extends Serializable> extends Serializable, Comparable<T>{
  
}
