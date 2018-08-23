package com.flockinger.groschn.blockchain.wallet;

import java.math.BigDecimal;
import java.security.PrivateKey;
import com.flockinger.groschn.blockchain.dto.WalletDto;
import com.flockinger.groschn.blockchain.dto.WalletSecretDto;

public interface WalletService {
  
  String getNodePublicKey();
  
  PrivateKey getPrivateKey(String publicKey, String secretKey);

  //TODO add calculate balance and stuff
  
  BigDecimal calculateBalance(String publicKey);
  
  WalletDto createWallet();
  WalletSecretDto fetchAndForgetWalletSecret(String publicKey);
}
