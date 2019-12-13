package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetBlockCount extends BitcoindResult {
	
	@JsonProperty("result")
	private Integer blockNumber;

	public Integer getBlockNumber() {
		return blockNumber;
	}

	public void setBlockNumber(Integer blockNumber) {
		this.blockNumber = blockNumber;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockCount [blockNumber=");
		builder.append(blockNumber);
		builder.append("]");
		return builder.toString();
	}
	
}
