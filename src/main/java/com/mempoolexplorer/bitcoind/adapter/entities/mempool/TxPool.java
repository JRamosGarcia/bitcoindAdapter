package com.mempoolexplorer.bitcoind.adapter.entities.mempool;

import java.util.Map;
import java.util.Set;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public interface TxPool {

	// Apply may change mempoolSequence or not.(i.e. changes for
	// connected/disconnected block)
	void apply(TxPoolChanges txPoolChanges, int mempoolSequence);

	void apply(TxPoolChanges txPoolChanges);

	// Also, we could be interested in changing only mempoolSequence
	void apply(int mempoolSequence);

	Set<String> getTxIdSet();

	// Returns null if Tx is not in the mempool
	Transaction getTx(String txId);

	// This map is a copy of the internal representation. Use with care! mempool can
	// be huge.
	Map<String, Transaction> getFullTxPool();

	int getSize();

	int getMempoolSequence();

	void drop();
}