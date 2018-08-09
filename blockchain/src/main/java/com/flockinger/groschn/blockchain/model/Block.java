package com.flockinger.groschn.blockchain.model;

import java.util.List;

import com.flockinger.groschn.blockchain.consensus.model.Consent;

public class Block implements Hashable {

	private Long position;
	private String hash;
	private String lastHash;
	private String transactionMerkleRoot;
	private Long timestamp;
	private Integer version;

	private Consent consent;

	private List<Transaction> transactions;

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
