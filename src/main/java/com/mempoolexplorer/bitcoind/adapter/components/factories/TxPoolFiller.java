package com.mempoolexplorer.bitcoind.adapter.components.factories;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.MemPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;

public interface TxPoolFiller {

	TxPool createMemPool() throws MemPoolException;

	TxPoolDiff obtainMemPoolDiffs(TxPool txPool) throws MemPoolException;

}
