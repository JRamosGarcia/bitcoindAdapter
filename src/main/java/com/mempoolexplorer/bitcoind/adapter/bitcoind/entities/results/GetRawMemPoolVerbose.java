package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRawMemPoolVerbose extends BitcoindResult {

	@JsonProperty("result")
	private Map<String, GetRawMemPoolVerboseData> getRawMemPoolVerboseDataMap;

	public GetRawMemPoolVerbose() {
		super();
	}

	public Map<String, GetRawMemPoolVerboseData> getGetRawMemPoolVerboseDataMap() {
		return getRawMemPoolVerboseDataMap;
	}

	public void setGetRawMemPoolVerboseDataMap(Map<String, GetRawMemPoolVerboseData> getRawMemPoolVerboseDataMap) {
		this.getRawMemPoolVerboseDataMap = getRawMemPoolVerboseDataMap;
	}

	@Override
	public String toString() {
		return "GetRawMemPoolVerbose [getRawMemPoolVerboseDataMap=" + getRawMemPoolVerboseDataMap + ", getError()="
				+ getError() + ", getId()=" + getId() + "]";
	}



}
