package com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.changes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindAdapterProperties;

@Component
public class InMemTxPoolChangesContainerImpl implements TxPoolChangesContainer {

	@Autowired
	BitcoindAdapterProperties props;

	private Collection<TxPoolChanges> circularFifoQueue;

	@PostConstruct
	public void init() {
		circularFifoQueue = Collections
				.synchronizedCollection(new CircularFifoQueue<TxPoolChanges>(props.getMemPoolChangesSize()));
	}

	@Override
	public void add(TxPoolChanges txPoolChanges) {
		circularFifoQueue.add(txPoolChanges);
	}

	@Override
	public List<TxPoolChanges> getLastChangesFrom(Instant instant) {
		List<TxPoolChanges> txPoolChangesList = new ArrayList<>();

		synchronized (circularFifoQueue) {
			Iterator<TxPoolChanges> i = circularFifoQueue.iterator(); // Must be in the synchronized block
			while (i.hasNext()) {
				TxPoolChanges memPoolChanges = i.next();
				if (memPoolChanges.getChangeTime().isAfter(instant.minusNanos(1))) {//Include the current instant
					txPoolChangesList.add(memPoolChanges);
				}
			}
		}
		return txPoolChangesList;
	}

	@Override
	public List<TxPoolChanges> getLastChangesFrom(Integer changeCounter) {
		List<TxPoolChanges> txPoolChangesList = new ArrayList<>();

		synchronized (circularFifoQueue) {
			Iterator<TxPoolChanges> i = circularFifoQueue.iterator(); // Must be in the synchronized block
			while (i.hasNext()) {
				TxPoolChanges txPoolChanges = i.next();
				if (txPoolChanges.getChangeCounter() >= changeCounter) {
					txPoolChangesList.add(txPoolChanges);
				}
			}
		}
		return txPoolChangesList;
	}

}
