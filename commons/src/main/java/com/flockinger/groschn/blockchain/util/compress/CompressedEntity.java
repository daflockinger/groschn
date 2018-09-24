package com.flockinger.groschn.blockchain.util.compress;

import java.io.Serializable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CompressedEntity implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 8539218059879786960L;
  
  @Min(1)
  private int originalSize;
  @Size(min=1)
  @NotNull
  private byte[] entity;
  
  public int getOriginalSize() {
    return originalSize;
  }
  public CompressedEntity originalSize(int originalSize) {
    this.originalSize = originalSize;
    return this;
  }
  public byte[] getEntity() {
    return entity;
  }
  public CompressedEntity entity(byte[] entity) {
    this.entity = entity;
    return this;
  }
  
  public static CompressedEntity build() {
    return new CompressedEntity();
  }
}
