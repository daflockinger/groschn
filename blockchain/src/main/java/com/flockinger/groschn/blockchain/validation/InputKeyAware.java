package com.flockinger.groschn.blockchain.validation;

import java.util.List;

public interface InputKeyAware {
  
  void addUsedInputPublicKeys(List<String> usedPublicKeys);
}
