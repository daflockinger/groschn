package com.flockinger.groschn.blockchain.util.sign.impl;

import java.security.KeyPair;
import java.security.PrivateKey;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.util.sign.Signer;

@Component("EDDSA_Signer")
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
