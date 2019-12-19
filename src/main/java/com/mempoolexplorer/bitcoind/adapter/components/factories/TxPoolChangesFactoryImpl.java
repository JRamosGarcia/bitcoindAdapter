package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

@Component
public class TxPoolChangesFactoryImpl implements TxPoolChangesFactory {

	@Autowired
	private Clock clock;

	private AtomicInteger changeCounter = new AtomicInteger(0);

	@Override
	public TxPoolChanges from(TxPoolDiff txPoolDiff) {
		TxPoolChanges txPoolChanges = new TxPoolChanges();
		txPoolChanges.setChangeTime(Instant.now(clock));
		// First change counter will be 1 since 0 si reserved for (re)start
		txPoolChanges.setChangeCounter(changeCounter.addAndGet(1));
		txPoolChanges.setNewTxs(new ArrayList<>(txPoolDiff.getNewMemPool().getFullTxPool().values()));
		txPoolChanges.setRemovedTxsId(new ArrayList<>(txPoolDiff.getGoneOrMinedMemPool().getTxIdSet()));

		return txPoolChanges;
	}

}
