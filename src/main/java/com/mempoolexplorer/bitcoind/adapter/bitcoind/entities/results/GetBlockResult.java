package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetBlockResult {

	@JsonProperty("result")
	private GetBlockResultData getBlockResultData;

	public GetBlockResultData getGetBlockResultData() {
		return getBlockResultData;
	}

	public void setGetBlockResultData(GetBlockResultData getBlockResultData) {
		this.getBlockResultData = getBlockResultData;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockResult [getBlockResultData=");
		builder.append(getBlockResultData);
		builder.append("]");
		return builder.toString();
	}

}
