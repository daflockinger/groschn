package com.flockinger.groschn.blockchain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;

public class Block implements Hashable<Block> {
  /**
  * 
  */
  private static final long serialVersionUID = -3738919661647370010L;

  private Long position = null;
  private String hash = null;
  private String lastHash = null;
  private String transactionMerkleRoot = null;
  private Long timestamp = null;
  private Integer version = null;

  private Consent consent = null;

  private List<Transaction> transactions = null;


  /**
   * Currently max transaction byte size is set to 100 kilobytes (compressed).
   */
  public final static Long MAX_TRANSACTION_BYTE_SIZE = 100l * 1024l;


  /**
   * Maximum amount of Groschn that can ever exist (are sixty hundred and fifty million Groschn).
   */
  public final static Long MAX_AMOUNT_MINED_GROSCHN = 650000000l;


  public final static Block GENESIS_BLOCK() {
    Block genesisBlock = new Block();
    genesisBlock.setLastHash("Wer den Groschen nicht ehrt, ist den Schilling nicht wert!");
    genesisBlock.setHash( //FIXME make that with correct leading zeros!
        "2c7a509afb7c6675774b75e999e8191c7790161da9843f16b7519cb756200e3cb6d7ea6b8d4078c4805d1205b415b9d83e5d5b0a10e16d9f70e8d1deef47a15e");
    genesisBlock.setTimestamp(484696800000l);
    genesisBlock.setTransactions(new ArrayList<>());
    genesisBlock.setPosition(1l);
    genesisBlock.setVersion(1);
    Consent powConsent = new Consent();
    powConsent.setType(ConsensusType.PROOF_OF_WORK);
    powConsent.setDifficulty(ProofOfWorkAlgorithm.DEFAULT_DIFFICULTY);
    powConsent.setMilliSecondsSpentMining(439l);
    powConsent.setTimestamp(1536407028239l);
    powConsent.setNonce(69532l);
    genesisBlock.setConsent(powConsent);
    return genesisBlock;
  }

  public String getTransactionMerkleRoot() {
    return transactionMerkleRoot;
  }

  public void setTransactionMerkleRoot(String transactionMerkleRoot) {
    this.transactionMerkleRoot = transactionMerkleRoot;
  }

  public Long getPosition() {
    return position;
  }

  public void setPosition(Long position) {
    this.position = position;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public String getLastHash() {
    return lastHash;
  }

  public void setLastHash(String lastHash) {
    this.lastHash = lastHash;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public List<Transaction> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<Transaction> transactions) {
    this.transactions = transactions;
  }

  public Consent getConsent() {
    return consent;
  }

  public void setConsent(Consent consent) {
    this.consent = consent;
  }

  @Override
  public int compareTo(Block o) {
    if (this.getPosition() == null && o.getPosition() == null) {
      return 0;
    } else if (this.getPosition() == null) {
      return -1;
    } else if (o.getPosition() == null) {
      return 1;
    }
    return this.getPosition().compareTo(o.getPosition());
  }

  @Override
  public String toString() {
    if(transactions != null) {
      Collections.sort(transactions);
    }
    return "Block [position=" + position + ", hash=" + hash + ", lastHash=" + lastHash
        + ", transactionMerkleRoot=" + transactionMerkleRoot + ", timestamp=" + timestamp
        + ", version=" + version + ", consent=" + consent + ", transactions=" + transactions + "]";
  }
}
