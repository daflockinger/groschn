package com.flockinger.groschn.blockchain.util;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
  private final ObjectMapper mapper = new ObjectMapper();
  private static final Logger LOGGER = LoggerFactory.getLogger(CompressionUtils.class);

  public <T extends Hashable> CompressedEntity compress(T entity) {
    byte[] originalEntity = entity.toByteArray();
    byte[] compressedEntity = compressor.compress(originalEntity);
    return CompressedEntity.build().
        originalSize(originalEntity.length)
        .entity(compressedEntity);
  }

  public <T extends Hashable> Optional<T> decompress(byte[] compressedEntity, int uncompressedSize, Class<T> type) {
    byte[] uncompressedEntityBytes = decompressor.decompress(compressedEntity, uncompressedSize);
    try {
      return Optional.ofNullable(mapper.readValue(uncompressedEntityBytes, type));
    } catch (IOException e) {
      LOGGER.error("Can't convert Json-byteArray to original Object back!", e);
    }
    return Optional.empty();
  }
}
