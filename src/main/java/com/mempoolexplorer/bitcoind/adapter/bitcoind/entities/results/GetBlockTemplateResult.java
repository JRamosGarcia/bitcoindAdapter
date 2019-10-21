package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetBlockTemplateResult extends BitcoindResult {

	@JsonProperty("result")
	private GetBlockTemplateResultData getBlockTemplateResultData;

	public GetBlockTemplateResultData getGetBlockTemplateResultData() {
		return getBlockTemplateResultData;
	}

	public void setGetBlockTemplateResultData(GetBlockTemplateResultData getBlockTemplateResultData) {
		this.getBlockTemplateResultData = getBlockTemplateResultData;
	}

	@Override
	public String toString() {
		return "GetBlockTemplateResult [getBlockTemplateResultData=" + getBlockTemplateResultData + ", getError()="
				+ getError() + ", getId()=" + getId() + "]";
	}

}
