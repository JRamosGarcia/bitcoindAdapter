package com.mempoolexplorer.bitcoind.adapter.components.factories;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public interface LongPoollingTxPoolFiller {

	TxPool createMemPool() throws TxPoolException;

	TxPoolChanges obtainMemPoolChanges(TxPool txPool) throws TxPoolException;

}
