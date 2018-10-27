package com.flockinger.groschn.commons.sign;

import java.security.KeyPair;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.commons.sign.Signer;

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
