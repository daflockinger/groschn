package com.flockinger.groschn.gateway.api;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import com.flockinger.groschn.messaging.members.NetworkStatistics;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = NodeStatisticsController.class)
public class NodeStatisticsControllerTest {
  
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  private NetworkStatistics statisticsMock;
  
  private MediaType jsonContentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
  
  @Test
  public void testGetActiveFullNodes_shouldCallNetworkStatistics() throws Exception {
    when(statisticsMock.activeFullNodes()).thenReturn(new ArrayList<>());
    
    mockMvc.perform(get("/nodes").contentType(jsonContentType))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", notNullValue()));
    
    verify(statisticsMock).activeFullNodes();
  }
  
}
