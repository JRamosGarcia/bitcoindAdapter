package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

public class GetBlockTemplateTransaction {

	private String txid;
	private long fee;
	private int sigops;
	private int weight;

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public long getFee() {
		return fee;
	}

	public void setFee(long fee) {
		this.fee = fee;
	}

	public int getSigops() {
		return sigops;
	}

	public void setSigops(int sigops) {
		this.sigops = sigops;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockTemplateTransaction [txid=");
		builder.append(txid);
		builder.append(", fee=");
		builder.append(fee);
		builder.append(", sigops=");
		builder.append(sigops);
		builder.append(", weight=");
		builder.append(weight);
		builder.append("]");
		return builder.toString();
	}

}
