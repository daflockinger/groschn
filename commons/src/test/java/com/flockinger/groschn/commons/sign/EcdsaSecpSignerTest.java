package com.flockinger.groschn.commons.sign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.flockinger.groschn.commons.TestConfig;
import com.flockinger.groschn.commons.exception.crypto.CantConfigureSigningAlgorithmException;
import com.flockinger.groschn.commons.hash.Base58;
import java.security.KeyPair;
import java.security.Signature;
import org.apache.commons.codec.DecoderException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Before;
import org.junit.Test;

public class EcdsaSecpSignerTest {

  private Signer signer = new EcdsaSecpSigner(TestConfig.getDefaultProvider());
  
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
    String signature = signer.sign(Hex.decode(someHash), pair.getPrivate().getEncoded());
    
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initVerify(pair.getPublic());
    signaturer.update(Hex.decode(someHash));
    assertTrue("check if signature is working correctly", signaturer.verify(Base58.decode(signature)));
  }
  
  @Test
  public void testSign_withEmptyByteArray_shouldSignCorrectly() throws DecoderException {
    String signature = signer.sign(new byte[0], pair.getPrivate().getEncoded());
    assertNotNull("verify that even signing nothing returns something", signature);
  }
  
  @Test(expected=CantConfigureSigningAlgorithmException.class)
  public void testSign_withInvalidKeyPair_shouldThrowException() throws DecoderException {
    String signature = signer.sign(new byte[0], new byte[0]);
    assertNotNull("verify that even signing nothing returns something", signature);
  }
  
  
  @Test
  public void testIsSignatureValid_withValidSignature_shouldReturnTrue() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decode(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decode(someHash), 
        Base58.encode(pair.getPublic().getEncoded()), Base58.encode(signature));
    
    assertEquals("verify that correct signature is valid", true, isValid);
  }
  
  @Test
  public void testIsSignatureValid_withInalidSignature_shouldReturnFalse() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(signer.generateKeyPair().getPrivate());
    signaturer.update(Hex.decode(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decode(someHash), 
        Base58.encode(pair.getPublic().getEncoded()), Base58.encode(signature));
    
    assertEquals("verify that manipulated signature is NOT valid", false, isValid);
  }
  
  @Test
  public void testIsSignatureValid_withInalidTransactionHash_shouldReturnFalse() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    final String someOtherHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EB";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decode(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decode(someOtherHash), 
        Base58.encode(pair.getPublic().getEncoded()), Base58.encode(signature));
    
    assertEquals("verify that manipulated signature is NOT valid", false, isValid);
  }
  
  @Test
  public void testIsSignatureValid_withWrongKey_shouldReturnFalse() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decode(someHash));
    byte[] signature = signaturer.sign();
    KeyPair freshPair = signer.generateKeyPair();
    
    boolean isValid = signer.isSignatureValid(Hex.decode(someHash), 
        Base58.encode(freshPair.getPublic().getEncoded()), Base58.encode(signature));
    
    assertEquals("verify that correct signature is valid", false, isValid);
  }
  
  @Test(expected=CantConfigureSigningAlgorithmException.class)
  public void testIsSignatureValid_withCompletlyWrongSignature_shouldThrowException() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decode(someHash));    
    boolean isValid = signer.isSignatureValid(Hex.decode(someHash), 
        Base58.encode(pair.getPublic().getEncoded()), "xoxoxo");
    
    assertEquals("verify that correct signature is valid", true, isValid);
  }
  
  @Test(expected=CantConfigureSigningAlgorithmException.class)
  public void testIsSignatureValid_withCompletlyWrongPubKey_shouldThrowException() throws Exception {
    final String someHash = "CCADD99B16CD3D200C22D6DB45D8B6630EF3D936767127347EC8A76AB992C2EA";
    Signature signaturer = Signature.getInstance(EcdsaSecpSigner.SIGNATURE_ALGORITHM, EcdsaSecpSigner.PROVIDER); 
    signaturer.initSign(pair.getPrivate());
    signaturer.update(Hex.decode(someHash));
    byte[] signature = signaturer.sign();
    
    boolean isValid = signer.isSignatureValid(Hex.decode(someHash), 
        "sEcReT", Base58.encode(signature));
    
    assertEquals("verify that correct signature is valid", true, isValid);
  }
}
