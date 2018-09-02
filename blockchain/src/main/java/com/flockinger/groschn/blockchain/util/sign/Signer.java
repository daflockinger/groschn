package com.flockinger.groschn.blockchain.util.sign;

import java.security.KeyPair;


public interface Signer {
  
  KeyPair generateKeyPair();
  
  String sign(byte[] transactionHash, byte[] privateKey);

  boolean isSignatureValid(byte[] transactionHash, String publicKey, String signature);
}
