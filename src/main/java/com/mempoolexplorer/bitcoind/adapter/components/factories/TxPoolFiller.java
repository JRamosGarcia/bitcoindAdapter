package com.mempoolexplorer.bitcoind.adapter.components.factories;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;

public interface TxPoolFiller {

	TxPool createMemPool() throws TxPoolException;

	TxPoolDiff obtainMemPoolDiffs(TxPool txPool) throws TxPoolException;

}
