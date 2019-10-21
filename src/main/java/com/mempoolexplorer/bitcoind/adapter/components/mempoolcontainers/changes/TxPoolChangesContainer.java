package com.mempoolexplorer.bitcoind.adapter.components.mempoolcontainers.changes;

import java.time.Instant;
import java.util.List;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public interface TxPoolChangesContainer {

	void add(TxPoolChanges memPoolChanges);

	List<TxPoolChanges> getLastChangesFrom(Instant instant);

	List<TxPoolChanges> getLastChangesFrom(Integer changeCounter);

}
