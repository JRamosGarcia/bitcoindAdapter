package com.mempoolexplorer.bitcoind.adapter.components.events.sources;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public interface TxSource {

	void publishTxChanges(TxPoolChanges txPoolChanges);

}