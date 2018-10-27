package com.flockinger.groschn.messaging.util;

import java.util.ArrayList;
import java.util.List;
import com.flockinger.groschn.blockchain.model.Hashable;

public class TestBlock implements Hashable<TestBlock> {
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

  private TestConsent consent;

  private List<TestTransaction> transactions;


  /**
   * Currently max transaction byte size is set to 100 kilobytes (compressed).
   */
  public final static Long MAX_TRANSACTION_BYTE_SIZE = 100l * 1024l;


  /**
   * Maximum amount of Groschn that can ever exist (are sixty hundred and fifty million Groschn).
   */
  public final static Long MAX_AMOUNT_MINED_GROSCHN = 650000000l;


  public final static TestBlock GENESIS_BLOCK() {
    TestBlock genesisBlock = new TestBlock();
    genesisBlock.setLastHash("Wer den Groschen nicht ehrt, ist den Schilling nicht wert!");
    genesisBlock.setHash(
        "253fe5a9b5754af105567ec7fc95fd8970701e6897f76ff77f7e9c3c97836f7b84a4b5813ee10be8462befbe08f301a8b93bf2bbc6a24cba4d7d2b52d50a8e5a");
    genesisBlock.setTimestamp(484696800000l);
    genesisBlock.setTransactions(new ArrayList<>());
    genesisBlock.setPosition(1l);
    genesisBlock.setVersion(1);
    TestConsent powConsent = new TestConsent();
    powConsent.setType(TestConsensusType.PROOF_OF_WORK);
    powConsent.setDifficulty(TestConsent.DEFAULT_DIFFICULTY);
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

  public List<TestTransaction> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<TestTransaction> transactions) {
    this.transactions = transactions;
  }

  public TestConsent getConsent() {
    return consent;
  }

  public void setConsent(TestConsent consent) {
    this.consent = consent;
  }

  @Override
  public int compareTo(TestBlock o) {
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
    return "TestBlock [position=" + position + ", hash=" + hash + ", lastHash=" + lastHash
        + ", transactionMerkleRoot=" + transactionMerkleRoot + ", timestamp=" + timestamp
        + ", version=" + version + ", consent=" + consent + ", transactions=" + transactions + "]";
  }
}
