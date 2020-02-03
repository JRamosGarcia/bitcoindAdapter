package com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results;

import java.util.List;

public class GetBlockTemplateResultData {

	private List<GetBlockTemplateTransaction> transactions;

	public List<GetBlockTemplateTransaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<GetBlockTemplateTransaction> transactions) {
		this.transactions = transactions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GetBlockTemplateResultData [transactions=");
		builder.append(transactions);
		builder.append("]");
		return builder.toString();
	}

}
