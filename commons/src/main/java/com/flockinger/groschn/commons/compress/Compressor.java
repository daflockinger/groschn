package com.flockinger.groschn.commons.compress;

import com.flockinger.groschn.blockchain.model.Hashable;
import com.flockinger.groschn.commons.exception.SerializationException;
import com.flockinger.groschn.commons.serialize.BlockSerializer;
import java.util.List;
import java.util.Optional;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Compressor {

  private final BlockSerializer serializer;
  private final LZ4Factory compressorFactory = LZ4Factory.fastestJavaInstance();
  private final LZ4Compressor compressor = compressorFactory.fastCompressor();
  private final LZ4FastDecompressor decompressor = compressorFactory.fastDecompressor();
  private static final Logger LOGGER = LoggerFactory.getLogger(Compressor.class);

  public Compressor(BlockSerializer serializer) {
    this.serializer = serializer;
  }

  public <T extends Hashable<T>> CompressedEntity compress(T entity) {
    byte[] originalEntity = serializer.serialize(entity);
    byte[] compressedEntity = compressor.compress(originalEntity);
    return CompressedEntity.build().originalSize(originalEntity.length).entity(compressedEntity);
  }


  public <T extends Hashable<T>> Optional<T> decompress(byte[] compressedEntity, int uncompressedSize,
      Class<T> type) {
    try {
      byte[] uncompressedEntityBytes = decompressor.decompress(compressedEntity, uncompressedSize);
      return Optional.ofNullable(serializer.deserialize(uncompressedEntityBytes, type));
    } catch (SerializationException e) {
      LOGGER.error("Can't deserialize entity back to original!", e);
    } catch (LZ4Exception e) {
      LOGGER.error("Cannot decompress invalid entity!",e);
    }
    return Optional.empty();
  }
  
  public <T extends Hashable<T>> int compressedByteSize(List<T> entities) {
    if(ListUtils.emptyIfNull(entities).isEmpty()) {
      return 0;
    }
    byte[] entityBytes = serializer.serialize(entities);
    return compressor.compress(entityBytes, new byte[entityBytes.length]);
  }

  public BlockSerializer serializer() {
    return  serializer;
  }
}
