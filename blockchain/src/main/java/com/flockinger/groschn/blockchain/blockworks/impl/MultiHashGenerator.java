package com.flockinger.groschn.blockchain.blockworks.impl;

import static com.flockinger.groschn.blockchain.config.CryptoConfig.DEFAULT_PROVIDER_NAME;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.jcajce.util.MessageDigestUtils;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.blockworks.HashGenerator;
import com.flockinger.groschn.blockchain.exception.HashingException;
import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.blockchain.model.Sequential;
import com.flockinger.groschn.blockchain.util.serialize.BlockSerializer;

/**
 * Generates a mixed SHA hash out of a {@link Hashable}. <br>
 * First it applies a SHA2-512 on the hashable String and <br>
 * on the resulting hash it finally applies a SHA3-512 which <br>
 * will be returned as final hash. <br>
 *
 */
@Component
public class MultiHashGenerator implements HashGenerator {

  private final static String SHA3_DIGEST_NAME = MessageDigestUtils.getDigestName(NISTObjectIdentifiers.id_sha3_512);
  private final static String SHA2_DIGEST_NAME = MessageDigestUtils.getDigestName(NISTObjectIdentifiers.id_sha512);
  
  private final MessageDigest sha3Digest;
  private final MessageDigest sha2Digest;
  
  @Autowired
  private BlockSerializer serializer;
  
  
  @Autowired
  public MultiHashGenerator(Provider cryptoProvider) {
    try {
      sha3Digest = MessageDigest.getInstance(SHA3_DIGEST_NAME, DEFAULT_PROVIDER_NAME);
      sha2Digest = MessageDigest.getInstance(SHA2_DIGEST_NAME, DEFAULT_PROVIDER_NAME);
    } catch (NoSuchAlgorithmException noAlgorithmException) {
      throw new HashingException("Essential hashing Algorithm not available!", noAlgorithmException);
    } catch (NoSuchProviderException noProviderException) {
      throw new HashingException("Security provider not existing/registered!", noProviderException);
    }
  }
  
  @Override
  public String generateHash(Hashable hashable) {
    var hashableBytes = serializer.serialize(hashable);
    assertHashable(hashableBytes);
    return Hex.toHexString(doubleHash(hashableBytes));
  }
  
  private byte[] doubleHash(byte[] hashableBytes) {
    var sha2Hash = hashWithDigest(hashableBytes, sha2Digest);   
    return hashWithDigest(sha2Hash, sha3Digest);
  }
  
  private void assertHashable(byte[] hashableBytes) {
    if(ArrayUtils.isEmpty(hashableBytes)) {
      throw new HashingException("Hashable bytes must not be empty!");
    }
  }
  
  private byte[] hashWithDigest(byte[] hashableMessage, MessageDigest digest) {
    digest.update(hashableMessage);
    return digest.digest();
  }

  @Override
  public boolean isHashCorrect(String hash, Hashable hashable) {    
    return StringUtils.equalsIgnoreCase(hash, generateHash(hashable));
  }

  @Override
  public <T extends Sequential> byte[] generateListHash(List<T> sortables) throws HashingException {
    Collections.sort(sortables);
    var hashableBytes = serializer.serialize(sortables);
    assertHashable(hashableBytes);  
    return doubleHash(hashableBytes);
  }
}
