package com.flockinger.groschn.blockchain.model;

import java.util.ArrayList;
import java.util.List;
import com.flockinger.groschn.blockchain.consensus.impl.ProofOfWorkAlgorithm;
import com.flockinger.groschn.blockchain.consensus.model.ConsensusType;
import com.flockinger.groschn.blockchain.consensus.model.Consent;

public class Block implements Hashable {
  /**
  * 
  */
  private static final long serialVersionUID = -3738919661647370010L;

  private Long position;
  private String hash;
  private String lastHash;
  private String transactionMerkleRoot;
  private Long timestamp;
  private Integer version;

  private Consent consent;

  private List<Transaction> transactions;


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
    genesisBlock.setHash(
        "00001b789b2d74adaa9c5e7e82e7f584fa30e6c52aa66683d6b44a456d1e9f9b9ac1f41ccbc76cc89503208e98735789356560e9389fd38664fabdd0bf2c543a");
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
}
