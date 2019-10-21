package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

public class GetBlockTemplateTransaction {

	private String txid;

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockTemplateTransaction [txid=");
		builder.append(txid);
		builder.append("]");
		return builder.toString();
	}

}
