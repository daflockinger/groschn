package com.flockinger.groschn.blockchain.api;

import java.io.IOException;
import java.nio.charset.Charset;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
public abstract class BaseControllerTest {
  
  protected MediaType jsonContentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  private final static ObjectMapper mapper = new ObjectMapper();

  @SuppressWarnings("unchecked")
  protected <T extends Object> String json(T entity) throws IOException {
    return mapper.writeValueAsString(entity);
  }
  
}
