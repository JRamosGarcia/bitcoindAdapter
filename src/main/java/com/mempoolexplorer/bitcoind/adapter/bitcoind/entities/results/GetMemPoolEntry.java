package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetMemPoolEntry extends BitcoindResult {
	@JsonProperty("result")
	RawMemPoolEntryData rawMemPoolEntryData;

	public RawMemPoolEntryData getRawMemPoolEntryData() {
		return rawMemPoolEntryData;
	}

	public void setRawMemPoolEntryData(RawMemPoolEntryData rawMemPoolEntryData) {
		this.rawMemPoolEntryData = rawMemPoolEntryData;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetMemPoolEntry [rawMemPoolEntryData=");
		builder.append(rawMemPoolEntryData);
		builder.append("]");
		return builder.toString();
	}

	
	
}
