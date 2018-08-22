package com.flockinger.groschn.blockchain.wallet;

import java.security.PrivateKey;

public interface WalletService {
  
  String getPublicKey();
  
  PrivateKey getPrivateKey();
  
  //TODO add calculate balance and stuff
}
