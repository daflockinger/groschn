package com.flockinger.groschn.commons.crypto;

import com.flockinger.groschn.commons.crypto.EncryptedKey;

public interface KeyCipher {

  EncryptedKey encrypt(byte[] key, byte[] passphrase);
  
  byte[] decrypt(EncryptedKey encryptedKey, byte[] passphrase);
  
  
  byte[] createPassphrase();
}
