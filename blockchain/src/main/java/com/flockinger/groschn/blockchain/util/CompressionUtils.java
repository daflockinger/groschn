package com.flockinger.groschn.blockchain.util;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flockinger.groschn.blockchain.model.Hashable;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

@Component
public class CompressionUtils {
  private final LZ4Factory compressorFactory = LZ4Factory.fastestJavaInstance();
  private final LZ4Compressor compressor = compressorFactory.fastCompressor();
  private final LZ4FastDecompressor decompressor = compressorFactory.fastDecompressor();
  private final ObjectMapper mapper;
  private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtils.class);

  public CompressionUtils() {
    mapper = new ObjectMapper();
    mapper.setDefaultPropertyInclusion(Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
  }


  public <T extends Hashable> CompressedEntity compress(T entity) {
    byte[] originalEntity = toByteArray(entity);
    byte[] compressedEntity = compressor.compress(originalEntity);
    return CompressedEntity.build().originalSize(originalEntity.length).entity(compressedEntity);
  }

  private byte[] toByteArray(Object hashable) {
    byte[] hashableBytes = new byte[0];
    try {
      hashableBytes = mapper.writeValueAsBytes(hashable);
    } catch (JsonProcessingException e) {
      LOGGER.error("Can't convert object to byteArray Json!", e);
    }
    return hashableBytes;
  }

  public <T extends Hashable> Optional<T> decompress(byte[] compressedEntity, int uncompressedSize,
      Class<T> type) {
    byte[] uncompressedEntityBytes = decompressor.decompress(compressedEntity, uncompressedSize);
    try {
      return Optional.ofNullable(mapper.readValue(uncompressedEntityBytes, type));
    } catch (IOException e) {
      LOGGER.error("Can't convert byteArray Json to original Object back!", e);
    }
    return Optional.empty();
  }
}
