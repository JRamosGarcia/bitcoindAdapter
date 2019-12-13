package com.mempoolexplorer.bitcoind.adapter.components.containers.blockchain.changes;

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

import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindAdapterProperties;

@Component
public class InMemLastBlocksContainerImpl implements LastBlocksContainer {

	@Autowired
	BitcoindAdapterProperties props;

	private Collection<Block> circularFifoQueue;

	@PostConstruct
	public void init() {
		circularFifoQueue = Collections
				.synchronizedCollection(new CircularFifoQueue<Block>(props.getNewBlockListSize()));
	}

	@Override
	public void add(Block block) {
		circularFifoQueue.add(block);
	}

	@Override
	public List<Block> getLastBlocksFrom(Instant instant) {
		List<Block> blockList = new ArrayList<>();

		synchronized (circularFifoQueue) {
			Iterator<Block> i = circularFifoQueue.iterator(); // Must be in the synchronized block
			while (i.hasNext()) {
				Block block = i.next();
				if (block.getChangeTime().isAfter(instant.minusNanos(1))) {//Include the current instant
					blockList.add(block);
				}
			}
		}
		return blockList;
	}

	@Override
	public List<Block> getLastBlocksFrom(Integer blockHeight) {
		List<Block> blockList = new ArrayList<>();

		synchronized (circularFifoQueue) {
			Iterator<Block> i = circularFifoQueue.iterator(); // Must be in the synchronized block
			while (i.hasNext()) {
				Block block = i.next();
				if (block.getHeight()>=blockHeight) {
					blockList.add(block);
				}
			}
		}
		return blockList;
	}

}
