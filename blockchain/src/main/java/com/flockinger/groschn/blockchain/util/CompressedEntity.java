package com.flockinger.groschn.blockchain.util;

public class CompressedEntity {
  private int originalSize;
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
