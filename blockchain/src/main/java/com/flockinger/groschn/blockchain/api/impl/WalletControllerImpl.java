package com.flockinger.groschn.blockchain.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.flockinger.groschn.blockchain.api.WalletController;
import com.flockinger.groschn.blockchain.api.dto.BalanceDto;
import com.flockinger.groschn.blockchain.wallet.WalletService;
import io.swagger.annotations.ApiParam;

@RestController
public class WalletControllerImpl implements WalletController {

  @Autowired
  private WalletService service;
  
  public ResponseEntity<BalanceDto> getBalance(
      @ApiParam(value = "Public key.", required = true) @PathVariable("pub-key") String pubKey) {
    
    BalanceDto balance = new BalanceDto();
    balance.setBalance(service.calculateBalance(pubKey).toString());
    return new ResponseEntity<BalanceDto>(balance, HttpStatus.OK);
  }
}
