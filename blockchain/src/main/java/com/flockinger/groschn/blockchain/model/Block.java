package com.flockinger.groschn.blockchain.model;

import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    genesisBlock.setHash("000094cdea7597ae9e2aa1643adb7a42c1912cc01fb95351df337760483b1e89f96f0f603da92b0f5b8e56886c77565160f759bf9fa82b19a3943ccd70a33247");
    genesisBlock.setTimestamp(484696800000L);
    genesisBlock.setTransactions(new ArrayList<>());
    genesisBlock.setPosition(1l);
    genesisBlock.setVersion(1);
    Consent powConsent = new Consent();
    powConsent.setType(ConsensusType.PROOF_OF_WORK);
    powConsent.setDifficulty(ProofOfWorkAlgorithm.DEFAULT_DIFFICULTY);
    powConsent.setMilliSecondsSpentMining(794L);
    powConsent.setTimestamp(1543396309902L);
    powConsent.setNonce(124688L);
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
