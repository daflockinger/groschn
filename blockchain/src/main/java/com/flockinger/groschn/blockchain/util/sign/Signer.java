package com.flockinger.groschn.blockchain.util.sign;

import java.security.KeyPair;
import java.security.PrivateKey;


public interface Signer {
  
  KeyPair generateKeyPair();
  
  String sign(byte[] transactionHash, PrivateKey privateKey);

  boolean isSignatureValid(byte[] transactionHash, String publicKey, String signature);
}
