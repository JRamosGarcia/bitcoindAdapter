package com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes;

public class CoinBaseTx {

	private String txId;
	private String vInField;
	private Integer sizeInvBytes;

	public CoinBaseTx() {
		super();
	}

	public CoinBaseTx(String txId, String vInField, Integer sizeInvBytes) {
		super();
		this.txId = txId;
		this.vInField = vInField;
		this.sizeInvBytes = sizeInvBytes;
	}

	public String getTxId() {
		return txId;
	}

	public void setTxId(String txId) {
		this.txId = txId;
	}

	public String getvInField() {
		return vInField;
	}

	public void setvInField(String vInField) {
		this.vInField = vInField;
	}

	public Integer getSizeInvBytes() {
		return sizeInvBytes;
	}

	public void setSizeInvBytes(Integer sizeInvBytes) {
		this.sizeInvBytes = sizeInvBytes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CoinBaseTx [txId=");
		builder.append(txId);
		builder.append(", vInField=");
		builder.append(vInField);
		builder.append(", sizeInvBytes=");
		builder.append(sizeInvBytes);
		builder.append("]");
		return builder.toString();
	}

}
