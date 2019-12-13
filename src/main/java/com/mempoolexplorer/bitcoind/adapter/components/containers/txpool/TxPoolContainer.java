package com.mempoolexplorer.bitcoind.adapter.components.containers.txpool;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.MemPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;

public interface TxPoolContainer {

	/**
	 * Create a txpool, which is returned.
	 * 
	 * @return
	 * @throws MemPoolException
	 */
	public void setTxPool(TxPool txPool) throws MemPoolException;

	/**
	 * gets the txpool contained in the Container.
	 * 
	 * @return
	 */
	public TxPool getTxPool();

}
