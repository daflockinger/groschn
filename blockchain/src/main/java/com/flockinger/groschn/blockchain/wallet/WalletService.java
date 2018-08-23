package com.flockinger.groschn.blockchain.wallet;

import java.math.BigDecimal;
import com.flockinger.groschn.blockchain.dto.UnlockedWalletDto;
import com.flockinger.groschn.blockchain.dto.WalletDto;
import com.flockinger.groschn.blockchain.exception.wallet.WalletNotFoundException;

public interface WalletService {
  
  String getNodePublicKey();
    
  byte[] getPrivateKey(String publicKey, String walletEncryptionKey);
  
  BigDecimal calculateBalance(String publicKey);
  
  WalletDto createWallet();
}
