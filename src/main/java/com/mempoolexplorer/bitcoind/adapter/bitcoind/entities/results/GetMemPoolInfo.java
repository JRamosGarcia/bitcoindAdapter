package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetMemPoolInfo extends BitcoindResult {

	@JsonProperty("result")
	private GetMemPoolInfoData getMemPoolInfoData;

	public GetMemPoolInfoData getGetMemPoolInfoData() {
		return getMemPoolInfoData;
	}

	public void setGetMemPoolInfoData(GetMemPoolInfoData getMemPoolInfoData) {
		this.getMemPoolInfoData = getMemPoolInfoData;
	}

	@Override
	public String toString() {
		return "GetMemPoolInfo [getMemPoolInfoData=" + getMemPoolInfoData + ", getError()=" + getError() + ", getId()="
				+ getId() + "]";
	}



}
