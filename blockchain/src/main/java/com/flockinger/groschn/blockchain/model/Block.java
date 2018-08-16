package com.flockinger.groschn.blockchain.model;

import java.util.ArrayList;
import java.util.List;
import com.flockinger.groschn.blockchain.consensus.model.Consent;
import com.flockinger.groschn.blockchain.consensus.model.PowConsent;

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

  public final static Block GENESIS_BLOCK() {
    Block genesisBlock = new Block();
    genesisBlock.setLastHash("Wer den Groschen nicht ehrt, ist den Schilling nicht wert!");
    genesisBlock.setHash(
        "0000cff71b99932db819f909cd56bc01c24b5ceefea2405a4d118fa18a208598c321a6e74b6ec75343318d18a253d866caa66a7a83cb7f241d295e3451115938");
    genesisBlock.setTimestamp(484696800000l);
    genesisBlock.setTransactions(new ArrayList<>());
    genesisBlock.setPosition(1l);
    genesisBlock.setVersion(1);
    PowConsent powConsent = new PowConsent();
    powConsent.setDifficulty(PowConsent.DEFAULT_DIFFICULTY);
    powConsent.setMilliSecondsSpentMining(PowConsent.MINING_RATE_MILLISECONDS);
    powConsent.setTimestamp(1533821027289l);
    powConsent.setNonce(7727l);
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
