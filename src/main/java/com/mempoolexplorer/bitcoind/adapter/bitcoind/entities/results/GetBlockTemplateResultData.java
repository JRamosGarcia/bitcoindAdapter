package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.List;

public class GetBlockTemplateResultData {

	private List<String> capabilities;
	private Integer version;
	private List<String> rules;
	private List<GetBlockTemplateTransaction> transactions;

	public List<String> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<String> capabilities) {
		this.capabilities = capabilities;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public List<GetBlockTemplateTransaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<GetBlockTemplateTransaction> transactions) {
		this.transactions = transactions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockTemplateResultData [capabilities=");
		builder.append(capabilities);
		builder.append(", version=");
		builder.append(version);
		builder.append(", rules=");
		builder.append(rules);
		builder.append(", transactions=");
		builder.append(transactions);
		builder.append("]");
		return builder.toString();
	}

}
