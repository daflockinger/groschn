package com.flockinger.groschn.commons;

import com.flockinger.groschn.commons.model.TestBlock;
import com.flockinger.groschn.commons.model.TestConsensusType;
import com.flockinger.groschn.commons.model.TestConsent;
import com.flockinger.groschn.commons.model.TestTransaction;
import com.flockinger.groschn.commons.model.TestTransactionInput;
import com.flockinger.groschn.commons.model.TestTransactionOutput;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import com.flockinger.groschn.commons.serialize.FstSerializer;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TestConfig {

  public static Provider getDefaultProvider() {
    Provider bouncyCastle = new BouncyCastleProvider();
    Security.addProvider(bouncyCastle);
    return bouncyCastle;
  }
  public static BlockSerializer serializer() {
    var registered = new ArrayList<Class<?>>();
    registered.add(TestBlock.class);
    registered.add(TestTransaction.class);
    registered.add(TestTransactionInput.class);
    registered.add(TestTransactionOutput.class);
    registered.add(TestConsent.class);
    registered.add(TestConsensusType.class);

    return new FstSerializer(registered);
  }
}
