package com.mempoolexplorer.bitcoind.adapter.components.containers.blockchain.changes;

import java.time.Instant;
import java.util.List;

import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;

public interface LastBlocksContainer {
	void add(Block block);

	List<Block> getLastBlocksFrom(Instant instant);

	List<Block> getLastBlocksFrom(Integer blockHeight);

}
