package com.flockinger.groschn.blockchain.util.crypto;

import static com.flockinger.groschn.blockchain.config.CommonsConfig.DEFAULT_PROVIDER_NAME;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.exception.crypto.CipherConfigurationException;

public class KeyAESCipher implements KeyCipher {
  private final static String CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding";
  public final static String ENCRYPTION_ALGORITHM = "AES";
  private final static String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
  private final static int AES_KEY_SIZE_BITS = 256;
  /**
   * Must be equal to the block size (128 bit for AES) of the algorithm
   */
  private final static int IV_SIZE_BYTES = 16;

  private final Cipher cipher;
  private final SecureRandom random;

  @Autowired
  public KeyAESCipher(Provider defaultProvider) {
    try {
      Security.addProvider(defaultProvider);
      cipher = Cipher.getInstance(CIPHER_TRANSFORMATION, DEFAULT_PROVIDER_NAME);
      random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM);
    } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
      throw new CipherConfigurationException(
          CIPHER_TRANSFORMATION + " is not supported by your system!", e);
    }
  }

  @Override
  public EncryptedKey encrypt(byte[] key, byte[] passphrase) throws CipherConfigurationException {
    try {
      IvParameterSpec initVector = createInitVector();
      cipher.init(Cipher.ENCRYPT_MODE, createKey(passphrase), initVector);
      byte[] encryptedKey = cipher.doFinal(key);
      
      return EncryptedKey.build()
          .key(encryptedKey).initVector(initVector.getIV());
    } catch (InvalidKeyException e) {
      throw new CipherConfigurationException("Key is not a valid AES 256bit key!", e);
    } catch (GeneralSecurityException e) {
      throw new CipherConfigurationException("Cipher configuration is not valid!", e);
    }
  }

  private Key createKey(byte[] passphrase) {
    return new SecretKeySpec(passphrase, 0, passphrase.length, ENCRYPTION_ALGORITHM);
  }

  private IvParameterSpec createInitVector() {
    byte[] ivCode = new byte[IV_SIZE_BYTES];
    random.nextBytes(ivCode);
    return new IvParameterSpec(ivCode);
}

  @Override
  public byte[] decrypt(EncryptedKey encryptedKey, byte[] passphrase)
      throws CipherConfigurationException {
    try {
      IvParameterSpec initVector = new IvParameterSpec(encryptedKey.getInitVector());
      cipher.init(Cipher.DECRYPT_MODE, createKey(passphrase), initVector);
      byte[] decryptedKey = cipher.doFinal(encryptedKey.getKey());
      
      return decryptedKey;
    } catch (InvalidKeyException e) {
      throw new CipherConfigurationException("Key is not a valid AES 256bit key!", e);
    } catch (GeneralSecurityException e) {
      throw new CipherConfigurationException("Cipher configuration is not valid!", e);
    }
  }


  @Override
  public byte[] createPassphrase() throws CipherConfigurationException {
    try {
      KeyGenerator generator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM, DEFAULT_PROVIDER_NAME);
      generator.init(AES_KEY_SIZE_BITS, random);
      SecretKey key = generator.generateKey();
      return key.getEncoded();
    } catch (NoSuchAlgorithmException e) {
      throw new CipherConfigurationException("Check your RNG! " 
          + SECURE_RANDOM_ALGORITHM + " not supported!", e);
    } catch (NoSuchProviderException e) {
      throw new CipherConfigurationException("BouncyCastle provider not setup correctly!", e);
    }
  }
}
