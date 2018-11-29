package com.flockinger.groschn.commons.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.flockinger.groschn.commons.TestConfig;
import com.flockinger.groschn.commons.exception.crypto.CipherConfigurationException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;

public class KeyAESCipherTest {

  private KeyCipher cipher = new KeyAESCipher(TestConfig.getDefaultProvider());
  
  @Test
  public void testEncryptDecryptCreatePassphrase_withValidKey_shouldWorkWell() {
    byte[] freshPassphrase = cipher.createPassphrase();
    assertEquals("verify created passphrase has exact length", 32, freshPassphrase.length);
    byte[] key = "very secret groschn key".getBytes(StandardCharsets.UTF_8);

    EncryptedKey encryptedKey = cipher.encrypt(key, freshPassphrase);
    assertNotNull("verify encrypted key is not null", encryptedKey);
    assertEquals("verify init vector has correct length", 16, encryptedKey.getInitVector().length);
    assertTrue("verify encrypted key is not empty", encryptedKey.getKey().length > 0);
    
    byte[] decryptedKey = cipher.decrypt(encryptedKey, freshPassphrase);
    assertTrue("verify that decrypted key is exactly the same as the original one", 
        Arrays.equals(key, decryptedKey));
  }
  
  @Test(expected=CipherConfigurationException.class)
  public void testDecrypt_withWrongButValidPassphrase_shouldFail() {
    byte[] freshPassphrase = cipher.createPassphrase();
    byte[] key = "very secret groschn key".getBytes(StandardCharsets.UTF_8);
    EncryptedKey encryptedKey = cipher.encrypt(key, freshPassphrase);
    
    cipher.decrypt(encryptedKey, cipher.createPassphrase());
  }
  
  @Test(expected = CipherConfigurationException.class)
  public void testEncrypt_withInvalidPassPhrase_shouldThrowException() {
    byte[] key = "very secret groschn key".getBytes(StandardCharsets.UTF_8);
    cipher.encrypt(key, "phrase".getBytes(StandardCharsets.UTF_8));
  }
  
  @Test
  public void testEncrypt_withEmptyKey_shouldNotFail() {
    byte[] freshPassphrase = cipher.createPassphrase();
    assertEquals("verify created passphrase has exact length", 32, freshPassphrase.length);

    EncryptedKey encryptedKey = cipher.encrypt(new byte[0], freshPassphrase);
    assertNotNull("verify encrypted key is not null", encryptedKey);
  }
  
  
  @Test(expected = CipherConfigurationException.class)
  public void testDecrypt_withInvalidInitVector_shouldThrowException() {
    byte[] freshPassphrase = cipher.createPassphrase();
    byte[] key = "very secret groschn key".getBytes(StandardCharsets.UTF_8);
    EncryptedKey encryptedKey = cipher.encrypt(key, freshPassphrase);
    
    cipher.decrypt(encryptedKey.initVector("a".getBytes()), freshPassphrase);
  }
  
  @Test(expected = CipherConfigurationException.class)
  public void testDecrypt_withMalformedKey_shouldThrowException() {
    byte[] freshPassphrase = cipher.createPassphrase();
    byte[] key = "very secret groschn key".getBytes(StandardCharsets.UTF_8);
    EncryptedKey encryptedKey = cipher.encrypt(key, freshPassphrase);
    
    cipher.decrypt(encryptedKey.key("a".getBytes()), freshPassphrase);
  }
  
  @Test(expected = CipherConfigurationException.class)
  public void testDecrypt_withWrongPassphrase_shouldThrowException() {
    byte[] freshPassphrase = cipher.createPassphrase();
    byte[] key = "very secret groschn key".getBytes(StandardCharsets.UTF_8);
    EncryptedKey encryptedKey = cipher.encrypt(key, freshPassphrase);
    
    cipher.decrypt(encryptedKey, "a".getBytes());
  }
}
