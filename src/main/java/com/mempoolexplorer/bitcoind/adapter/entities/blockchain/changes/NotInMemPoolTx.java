package com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes;

public class NotInMemPoolTx {

	private String txId;
	private Long fees;// in Satoshis. Sadly this does not take into account Ancestors
	private Integer vSize;// Sadly this does not take into account Ancestors

	public NotInMemPoolTx(String txId, Long fees, Integer vSize) {
		super();
		this.txId = txId;
		this.fees = fees;
		this.vSize = vSize;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public Long getFees() {
		return fees;
	}

	public void setFees(Long fees) {
		this.fees = fees;
	}

	public Integer getvSize() {
		return vSize;
	}

	public void setvSize(Integer vSize) {
		this.vSize = vSize;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotInMemPoolTx [txId=");
		builder.append(txId);
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", vSize=");
		builder.append(vSize);
		builder.append("]");
		return builder.toString();
	}

}