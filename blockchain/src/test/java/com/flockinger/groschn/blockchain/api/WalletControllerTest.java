package com.flockinger.groschn.blockchain.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import com.flockinger.groschn.blockchain.api.impl.WalletControllerImpl;
import com.flockinger.groschn.blockchain.wallet.WalletService;

@WebMvcTest(controllers = {WalletControllerImpl.class})
public class WalletControllerTest extends BaseControllerTest {

  @Autowired
  private MockMvc mockMvc;
  
  @MockBean
  private WalletService service;
  
  @Test
  public void testGetBalance_withValidKey_shouldReturnCorrect() throws Exception {
    when(service.calculateBalance(any())).thenReturn(new BigDecimal("1.12"));

    mockMvc.perform(get("/api/v1/groschn/wallet/balance/1234").contentType(jsonContentType))
        .andExpect(status().isOk()).andExpect(jsonPath("$.balance", is("1.12")));

    verify(service).calculateBalance(matches("1234"));
  }
  
  @Test
  public void testGetBalance_withInvalidKey_shouldReturnNotFound() throws Exception {
    when(service.calculateBalance(any())).thenReturn(BigDecimal.ZERO);

    mockMvc.perform(get("/api/v1/groschn/wallet/balance/1234").contentType(jsonContentType))
        .andExpect(status().isOk()).andExpect(jsonPath("$.balance", is("0")));
  }
}
