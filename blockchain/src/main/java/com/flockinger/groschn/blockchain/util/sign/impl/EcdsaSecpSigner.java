package com.flockinger.groschn.blockchain.util.sign.impl;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.flockinger.groschn.blockchain.exception.CantConfigureSigningAlgorithmException;
import com.flockinger.groschn.blockchain.util.sign.Signer;

@Component("ECDSA_Signer")
public class EcdsaSecpSigner implements Signer {
  /**
   * Elliptic Curve Digital Signature Algorithm
   * 
   * @see <a href="https://en.bitcoin.it/wiki/Elliptic_Curve_Digital_Signature_Algorithm">Bitcoin
   *      ECDSA doc</a>
   */
  public final static String SIGNATURE_ALGORITHM = "NONEwithECDSA";

  /**
   * Keys for the Elliptic Curve algorithm.
   * 
   * @see <a href=
   *      "https://arstechnica.com/information-technology/2013/10/a-relatively-easy-to-understand-primer-on-elliptic-curve-cryptography/">
   *      Info to understand Elliptic curve cryptography</a>
   */
  public final static String KEY_FACTORY_ALGORITHM = "EC";

  /**
   * Same as Bitcoin is using. Because it seems to be more secure than the random secp256r1:
   * 
   * @see <a href="https://chrispacia.wordpress.com/2013/10/30/nsa-backdoors-and-bitcoin/">Why
   *      Bitcoin uses secp256k1</a>
   */
  public final static String EC_GEN_PARAMETER_SPEC = "secp256k1";

  
  /**
   * Crypto Provider should be BouncyCastle cause the name sounds nice, and also cause everyone's using it.
   */
  public final static String PROVIDER = BouncyCastleProvider.PROVIDER_NAME;
  

  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  private KeyPairGenerator generator;
  private KeyFactory keyFactory;

  @Autowired
  public EcdsaSecpSigner(Provider defaultProvider) {
    try {
      Security.addProvider(defaultProvider);
      keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM, PROVIDER);
      generator = KeyPairGenerator.getInstance(KEY_FACTORY_ALGORITHM, PROVIDER);
      generator.initialize(getSpec(), new SecureRandom());
    } catch (InvalidAlgorithmParameterException e) {
      LOGGER.error("Elliptic Curve Algorithm parameters are not valid!", e);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("No EC Algorithm available for your system!", e);
    } catch (NoSuchProviderException e) {
      LOGGER.error("Please register bouncy castle provider before initializing!", e);
    }
  }

  private Signature getSignature() {
    try {
      return Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new CantConfigureSigningAlgorithmException("Cant configure signature algorithm NONEwithECDSA or BouncyCastle!");
    }
  }

  public KeyPair generateKeyPair() {
    if (generator == null) {
      throw new CantConfigureSigningAlgorithmException("Cant configure EC algorithm with secp256k1 spec!");
    }
    return generator.generateKeyPair();
  }

  private ECGenParameterSpec getSpec() {
    return new ECGenParameterSpec(EC_GEN_PARAMETER_SPEC);
  }

  public String sign(byte[] transactionHash, KeyPair keypair) {
    String signature;
    var signer = getSignature();
    try {
      signer.initSign(keypair.getPrivate());
      signer.update(transactionHash);
      signature = Hex.encodeHexString(signer.sign());
    } catch (InvalidKeyException | SignatureException e) {
      throw new CantConfigureSigningAlgorithmException("Signing didn't work out well!");
    }
    return signature;
  }

  public boolean isSignatureValid(byte[] transactionHash, String publicKey, String signature) {
    var isSignValid = false;
    var verifier = getSignature();
    try {
      verifier.initVerify(getPublicKeyFromHexDecodedText(publicKey));
      verifier.update(transactionHash);
      isSignValid = verifier.verify(getBytesFromHexEncodedString(signature));
    } catch (InvalidKeyException e) {
      throw new CantConfigureSigningAlgorithmException("Public Key is invalid!");
    } catch (SignatureException e) {
      throw new CantConfigureSigningAlgorithmException("Something's wrong with the signature!");
    }
    return isSignValid;
  }

  private PublicKey getPublicKeyFromHexDecodedText(String publicKeyHexDecoded) {
    PublicKey key = null;
    try {
      var pubKeyParams = (ECPublicKeyParameters) PublicKeyFactory
          .createKey(getBytesFromHexEncodedString(publicKeyHexDecoded));
      var parameterSpec = new ECParameterSpec(pubKeyParams.getParameters().getCurve(),
          pubKeyParams.getParameters().getG(), pubKeyParams.getParameters().getN());
      key = keyFactory.generatePublic(new ECPublicKeySpec(pubKeyParams.getQ(), parameterSpec));
    } catch (InvalidKeySpecException e) {
      throw new CantConfigureSigningAlgorithmException("Key Spec is invalid, please check!");
    } catch (IOException e) {
      throw new CantConfigureSigningAlgorithmException(
          "Error reading publicKey text, maybe the encoded value is invalid and therefore null or empty!",e);
    }
    return key;
  }

  private byte[] getBytesFromHexEncodedString(String hexEncodedText) {
    var decodedTextBytes = new byte[0];
    try {
      decodedTextBytes = Hex.decodeHex(hexEncodedText.toCharArray());
    } catch (DecoderException e) {
      throw new CantConfigureSigningAlgorithmException("Can't decode text, it's maybe not hex encoded!",e);
    }
    return decodedTextBytes;
  }
}