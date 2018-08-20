package com.flockinger.groschn.blockchain.validation.impl;

import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.model.Block;
import com.flockinger.groschn.blockchain.validation.Assessment;
import com.flockinger.groschn.blockchain.validation.Validator;

@Component
public class BlockValidator implements Validator<Block> {

  
  @Override
  public Assessment validate(Block value) {
    return null;
  }
}
