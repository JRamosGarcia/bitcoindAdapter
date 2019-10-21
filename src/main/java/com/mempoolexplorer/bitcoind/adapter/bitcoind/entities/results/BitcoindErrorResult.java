package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BitcoindErrorResult extends BitcoindResult {

	@JsonProperty("result")
	private String result = null;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

}
