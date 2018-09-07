package com.flockinger.groschn.blockchain.dto;

import java.io.Serializable;

public class BlockMessage implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1278143685516903764L;
  
  private String senderId;
  private byte[] compressedBlockHash;
  private byte[] compressedBlock;
  
  public String getSenderId() {
    return senderId;
  }
  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }
  public byte[] getCompressedBlockHash() {
    return compressedBlockHash;
  }
  public void setCompressedBlockHash(byte[] compressedBlockHash) {
    this.compressedBlockHash = compressedBlockHash;
  }
  public byte[] getCompressedBlock() {
    return compressedBlock;
  }
  public void setCompressedBlock(byte[] compressedBlock) {
    this.compressedBlock = compressedBlock;
  }
}
