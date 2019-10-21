package com.mempoolexplorer.bitcoind.adapter.entities.mempool;

import java.util.Map;
import java.util.Set;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;

public interface TxPool {

	void apply(TxPoolDiff memPoolDiff);

	Set<String> getTxIdSet();

	Transaction getTx(String txId);

	// This map is a copy of the internal representation. Use with care! mempool can
	// be huge.
	Map<String, Transaction> getFullTxPool();

	Integer getSize();
}