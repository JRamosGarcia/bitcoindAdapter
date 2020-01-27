package com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes;

public class NotInMemPoolTx {

	private String txId;
	private Long fees;// in Satoshis. Sadly this does not take into account Ancestors
	private Integer weigth;// Sadly this does not take into account Ancestors

	public NotInMemPoolTx(String txId, Long fees, Integer weigth) {
		super();
		this.txId = txId;
		this.fees = fees;
		this.weigth = weigth;
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

	public Integer getWeigth() {
		return weigth;
	}

	public void setWeigth(Integer weigth) {
		this.weigth = weigth;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NotInMemPoolTx [txId=");
		builder.append(txId);
		builder.append(", fees=");
		builder.append(fees);
		builder.append(", weigth=");
		builder.append(weigth);
		builder.append("]");
		return builder.toString();
	}

}