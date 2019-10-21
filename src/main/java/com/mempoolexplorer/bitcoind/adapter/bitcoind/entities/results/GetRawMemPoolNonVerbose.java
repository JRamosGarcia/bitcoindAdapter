package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRawMemPoolNonVerbose extends BitcoindResult {

	@JsonProperty("result")
	List<String> trxHashList;

	public List<String> getTrxHashList() {
		return trxHashList;
	}

	public void setTrxHashList(List<String> trxHashList) {
		this.trxHashList = trxHashList;
	}

	@Override
	public String toString() {
		return "GetRawMemPoolNonVerbose [trxHashList=" + trxHashList + ", getError()=" + getError() + ", getId()="
				+ getId() + "]";
	}

}
