package com.flockinger.groschn.blockchain.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HashUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(HashUtils.class);
  private static ObjectMapper mapper = new ObjectMapper();

  public static byte[] toByteArray(Object hashable) {
    String hashableJson = "";
    try {
      hashableJson = mapper.writeValueAsString(hashable);
    } catch (JsonProcessingException e) {
      LOGGER.error("Can't convert object to Json for byteArray creation!", e);
    }
    return hashableJson.getBytes();
  }
}
