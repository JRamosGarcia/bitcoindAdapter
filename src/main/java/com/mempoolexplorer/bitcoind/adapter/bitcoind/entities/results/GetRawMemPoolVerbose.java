package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRawMemPoolVerbose extends BitcoindResult {

	@JsonProperty("result")
	private Map<String, RawMemPoolEntryData> rawMemPoolEntryDataMap;

	public GetRawMemPoolVerbose() {
		super();
	}

	public Map<String, RawMemPoolEntryData> getRawMemPoolEntryDataMap() {
		return rawMemPoolEntryDataMap;
	}

	public void setRawMemPoolEntryDataMap(Map<String, RawMemPoolEntryData> rawMemPoolEntryDataMap) {
		this.rawMemPoolEntryDataMap = rawMemPoolEntryDataMap;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetRawMemPoolVerbose [rawMemPoolEntryDataMap=");
		builder.append(rawMemPoolEntryDataMap);
		builder.append("]");
		return builder.toString();
	}





}
