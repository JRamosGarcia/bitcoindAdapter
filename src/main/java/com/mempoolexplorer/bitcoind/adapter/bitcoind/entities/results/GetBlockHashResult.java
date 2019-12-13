package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetBlockHashResult extends BitcoindResult{

	@JsonProperty("result")
	private String blockHash;

	public String getBlockHash() {
		return blockHash;
	}

	public void setBlockHash(String blockHash) {
		this.blockHash = blockHash;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockHash [blockHash=");
		builder.append(blockHash);
		builder.append("]");
		return builder.toString();
	}
}
