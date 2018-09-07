package com.flockinger.groschn.blockchain.util.crypto;

public interface KeyCipher {

  EncryptedKey encrypt(byte[] key, byte[] passphrase);
  
  byte[] decrypt(EncryptedKey encryptedKey, byte[] passphrase);
  
  
  byte[] createPassphrase();
}
