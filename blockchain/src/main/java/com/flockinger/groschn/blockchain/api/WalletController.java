package com.flockinger.groschn.blockchain.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.flockinger.groschn.blockchain.api.dto.BalanceDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public interface WalletController {
  @ApiOperation(value = "Available Balance of for a Public-Key.", nickname = "getBalance", notes = "Returns currently available Balance of for a Public-Key.", response = BalanceDto.class, tags={ "Wallet", })
  @ApiResponses(value = { 
      @ApiResponse(code = 200, message = "Current available Balance.", response = BalanceDto.class),
      @ApiResponse(code = 400, message = "Bad request (validation failed).", response = Error.class),
      @ApiResponse(code = 403, message = "Forbidden (no rights to access resource)."),
      @ApiResponse(code = 404, message = "Entity not found.", response = Error.class),
      @ApiResponse(code = 409, message = "Request results in a conflict.", response = Error.class) })
  @RequestMapping(value = "/api/v1/groschn/wallet/balance/{pub-key}",
      produces = { "application/json" }, 
      method = RequestMethod.GET)
  ResponseEntity<BalanceDto> getBalance(@ApiParam(value = "Public key.",required=true) @PathVariable("pub-key") String pubKey);
}
