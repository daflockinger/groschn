package com.flockinger.groschn.blockchain.model;

import com.flockinger.groschn.blockchain.util.HashUtils;

public interface Hashable {
  
  default byte[] toByteArray() {
    return HashUtils.toByteArray(this);
  }
}
