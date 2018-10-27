package com.flockinger.groschn.blockchain.api.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.flockinger.groschn.blockchain.api.WalletController;
import com.flockinger.groschn.blockchain.api.dto.BalanceDto;
import io.swagger.annotations.ApiParam;

@RestController
public class WalletControllerImpl implements WalletController {

  public ResponseEntity<BalanceDto> getBalance(
      @ApiParam(value = "Public key.", required = true) @PathVariable("pub-key") String pubKey) {
    return new ResponseEntity<BalanceDto>(HttpStatus.NOT_IMPLEMENTED);
  }

}
