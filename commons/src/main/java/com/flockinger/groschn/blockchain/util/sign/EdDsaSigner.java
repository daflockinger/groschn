package com.flockinger.groschn.blockchain.util.sign;

import java.security.KeyPair;
import org.springframework.stereotype.Component;

public class EdDsaSigner implements Signer {

  @Override
  public KeyPair generateKeyPair() {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public String sign(byte[] transactionHash, byte[] privateKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isSignatureValid(byte[] transactionHash, String publicKey, String signature) {
    // TODO Auto-generated method stub
    return false;
  }

}
