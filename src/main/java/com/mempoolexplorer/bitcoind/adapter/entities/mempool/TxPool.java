package com.mempoolexplorer.bitcoind.adapter.entities.mempool;

import java.util.Map;
import java.util.Set;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public interface TxPool {

	// Apply may change mempoolSequence or not.(i.e. changes for connected/disconnected block)
	void apply(TxPoolChanges txPoolChanges, int mempoolSequence);

	void apply(TxPoolChanges txPoolChanges);

	Set<String> getTxIdSet();

	Transaction getTx(String txId);

	// This map is a copy of the internal representation. Use with care! mempool can
	// be huge.
	Map<String, Transaction> getFullTxPool();

	int getSize();

	int getMempoolSequence();

	void drop();
}