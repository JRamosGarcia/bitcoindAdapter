package com.mempoolexplorer.bitcoind.adapter.services;

import java.util.Optional;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public interface TxPoolService {

	Optional<TxPool> loadTxPoolFromDB();

	void saveAllMemPool(TxPool txPool);

	void apply(TxPoolChanges txpc);
}
