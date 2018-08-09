package com.flockinger.groschn.blockchain.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Objects;
import org.junit.Test;
import com.flockinger.groschn.blockchain.model.Transaction;

public class HashUtilsTest {
  
  @Test
  public void testToByteArray_withSomeObject_shouldConvert() {
    Transaction tra = new Transaction();
    tra.setId("123");
    tra.setInputs(new ArrayList<>());
    tra.setOutputs(new ArrayList<>());
    tra.setLockTime(null);
    
    byte[] result = HashUtils.toByteArray(tra);
    
    assertTrue("verify that the resulting byte array is not empty", result.length > 0);
    
    tra.setId("12");
    assertFalse("verify that slight changes result in not the same byte arrays", 
        Objects.deepEquals(result, HashUtils.toByteArray(tra)));
  }
  
  @Test
  public void testToByteArray_withSomeEmptyObject_shouldConvert() {
    Transaction tra = new Transaction();
    
    byte[] result = HashUtils.toByteArray(tra);
    
    assertTrue("verify that the resulting byte array is not empty", result.length > 0);
  }
}
