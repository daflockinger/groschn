package com.flockinger.groschn.blockchain.repository.model;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.flockinger.groschn.blockchain.consensus.model.Consent;

@Document(collection="blockchain")
@CompoundIndexes({
  @CompoundIndex(name="idx_transaction_hash", def= "{'transactions.transactionHash': 1}", background=true),
  @CompoundIndex(name="idx_tx_input_addr_pos_sorted", def= "{'transactions.inputs.publicKey': 1, 'position': -1}", background=true),
  @CompoundIndex(name="idx_tx_output_addr_pos_sorted", def= "{'transactions.outputs.publicKey': 1, 'position': -1}", background=true),
})
public class StoredBlock {

  @Id
  private String id = null;
  
  @Indexed
  @NotNull
  private Long position = null;
  
  @Indexed
  @NotNull
  private String hash = null;
  
  @NotNull
  private String lastHash = null;
  
  @Indexed
  @NotNull
  private Long timestamp = null;
  
  private String transactionMerkleRoot = null;
  
  private Integer version = null;
  
  private List<StoredTransaction> transactions = null;
  
  private Consent consent = null;

  public Consent getConsent() {
    return consent;
  }

  public void setConsent(Consent consent) {
    this.consent = consent;
  }

  public List<StoredTransaction> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<StoredTransaction> transactions) {
    this.transactions = transactions;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public String getTransactionMerkleRoot() {
    return transactionMerkleRoot;
  }

  public void setTransactionMerkleRoot(String transactionMerkleRoot) {
    this.transactionMerkleRoot = transactionMerkleRoot;
  }
}
