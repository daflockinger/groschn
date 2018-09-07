package com.flockinger.groschn.blockchain.model;

import java.io.Serializable;
import com.flockinger.groschn.blockchain.util.HashUtils;

public interface Hashable extends Serializable{
  
  default byte[] toByteArray() {
    return HashUtils.toByteArray(this);
  }
}
