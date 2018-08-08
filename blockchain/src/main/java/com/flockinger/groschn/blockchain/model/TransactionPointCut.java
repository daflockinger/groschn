package com.flockinger.groschn.blockchain.model;

public class TransactionPointCut {
	
	private String transactionHash;
	private Long sequenceNumber;
	
	public String getTransactionHash() {
		return transactionHash;
	}
	public void setTransactionHash(String transactionHash) {
		this.transactionHash = transactionHash;
	}
	public Long getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
}
