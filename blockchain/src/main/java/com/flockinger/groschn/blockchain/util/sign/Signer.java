package com.flockinger.groschn.blockchain.util.sign;

import java.security.KeyPair;


public interface Signer {
  
  KeyPair generateKeyPair();
  
  String sign(byte[] transactionHash, KeyPair keypair);

  boolean isSignatureValid(byte[] transactionHash, String publicKey, String signature);
}
