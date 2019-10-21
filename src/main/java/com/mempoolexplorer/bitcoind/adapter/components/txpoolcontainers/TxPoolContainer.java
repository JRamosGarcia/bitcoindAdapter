package com.mempoolexplorer.bitcoind.adapter.components.txpoolcontainers;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.MemPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;

public interface TxPoolContainer {

	/**
	 * Create a txpool, which is returned.
	 * 
	 * @return
	 * @throws MemPoolException
	 */
	public TxPool createTxPool() throws MemPoolException;

	/**
	 * refresh the txPool and returns the differences before and after the refresh
	 * 
	 * @return
	 * @throws MemPoolException
	 */
	public TxPoolDiff refreshTxPool() throws MemPoolException;

	/**
	 * gets the txpool contained in the Container.
	 * 
	 * @return
	 */
	public TxPool getTxPool();

}
