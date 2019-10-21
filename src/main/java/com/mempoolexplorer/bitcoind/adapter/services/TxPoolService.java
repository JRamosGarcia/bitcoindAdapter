package com.mempoolexplorer.bitcoind.adapter.services;

import java.util.Optional;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;

public interface TxPoolService {

	Optional<TxPoolDiff> loadTxPoolFromDB();

	void saveAllMemPool(TxPool txPool);

	void apply(TxPoolDiff diff);
}
