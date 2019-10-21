package com.mempoolexplorer.bitcoind.adapter.components.factories;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public interface TxPoolChangesFactory {

	TxPoolChanges from(TxPoolDiff txPoolDiff);
}
