package com.flockinger.groschn.blockchain.util.sign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.exception.crypto.CantConfigureSigningAlgorithmException;
import com.flockinger.groschn.blockchain.util.sign.impl.EcdsaSecpSigner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EcdsaSecpSigner.class})
@Import(CryptoConfig.class)
public class EcdsaSecpSignerTest {

  @Autowired
  @Qualifier("ECDSA_Signer")
  private Signer signer;
  
  private KeyPair pair;
  
  @Before
  public void setup() {
    pair = signer.generateKeyPair();
  }
  
  @Test
  public void testGenerateKeyPair_shouldGenerateCorrectly() throws Exception {    
    assertNotNull("verify generated pair is not null", pair);
    
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update("very important message".getBytes());
    byte[] signature = signaturer.sign();
    signaturer.initVerify(pair.getPublic());
    signaturer.update("very important message".getBytes());
    
    assertTrue("check if the keypair is really valid and works", signaturer.verify(signature));
  }
  
  @Test
  public void testSign_withAllSet_shouldSignCorrectly() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    String signature = signer.sign(Hex.decodeHex(someHash), pair.getPrivate());
    
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initVerify(pair.getPublic());
    signaturer.update(Hex.decodeHex(someHash));
    assertTrue("check if signature is working correctly", signaturer.verify(Hex.decodeHex(signature)));
  }
  
  @Test
  public void testSign_withEmptyByteArray_shouldSignCorrectly() throws DecoderException {
    String signature = signer.sign(new byte[0], pair.getPrivate());
    assertNotNull("verify that even signing nothing returns something", signature);
  }
  
  @Test(expected=CantConfigureSigningAlgorithmException.class)
  public void testSign_withInvalidKeyPair_shouldThrowException() throws DecoderException {
    String signature = signer.sign(new byte[0],  new PrivateKey() {
      public String getFormat() {return null;}
      public byte[] getEncoded() {return new byte[0];}
      public String getAlgorithm() {return null;}
    });
    assertNotNull("verify that even signing nothing returns something", signature);
  }
  
  
  @Test
  public void testIsSignatureValid_withValidSignature_shouldReturnTrue() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decodeHex(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decodeHex(someHash), 
        Hex.encodeHexString(pair.getPublic().getEncoded()), Hex.encodeHexString(signature));
    
    assertEquals("verify that correct signature is valid", true, isValid);
  }
  
  @Test
  public void testIsSignatureValid_withInalidSignature_shouldReturnFalse() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(signer.generateKeyPair().getPrivate());
    signaturer.update(Hex.decodeHex(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decodeHex(someHash), 
        Hex.encodeHexString(pair.getPublic().getEncoded()), Hex.encodeHexString(signature));
    
    assertEquals("verify that manipulated signature is NOT valid", false, isValid);
  }
  
  @Test
  public void testIsSignatureValid_withInalidTransactionHash_shouldReturnFalse() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    final String someOtherHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EB";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decodeHex(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decodeHex(someOtherHash), 
        Hex.encodeHexString(pair.getPublic().getEncoded()), Hex.encodeHexString(signature));
    
    assertEquals("verify that manipulated signature is NOT valid", false, isValid);
  }
  
  @Test
  public void testIsSignatureValid_withWrongKey_shouldReturnFalse() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decodeHex(someHash));
    byte[] signature = signaturer.sign();
    KeyPair freshPair = signer.generateKeyPair();
    
    boolean isValid = signer.isSignatureValid(Hex.decodeHex(someHash), 
        Hex.encodeHexString(freshPair.getPublic().getEncoded()), Hex.encodeHexString(signature));
    
    assertEquals("verify that correct signature is valid", false, isValid);
  }
  
  @Test(expected=CantConfigureSigningAlgorithmException.class)
  public void testIsSignatureValid_withCompletlyWrongSignature_shouldThrowException() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decodeHex(someHash));    
    boolean isValid = signer.isSignatureValid(Hex.decodeHex(someHash), 
        Hex.encodeHexString(pair.getPublic().getEncoded()), "xoxoxo");
    
    assertEquals("verify that correct signature is valid", true, isValid);
  }
  
  @Test(expected=CantConfigureSigningAlgorithmException.class)
  public void testIsSignatureValid_withCompletlyWrongPubKey_shouldThrowException() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decodeHex(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decodeHex(someHash), 
        "sEcReT", Hex.encodeHexString(signature));
    
    assertEquals("verify that correct signature is valid", true, isValid);
  }
}
