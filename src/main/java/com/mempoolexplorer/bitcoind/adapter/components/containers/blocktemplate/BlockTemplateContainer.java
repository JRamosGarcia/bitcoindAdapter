package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import java.util.List;
import java.util.Optional;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

/**
 * Stores BlockTemplates for different block Height.
 */
public interface BlockTemplateContainer {

	// insert or replace blockTemplate for a block height
	void push(BlockTemplate blockTemplate);

	// Returns BlockTemplate for height if any and remove it from map.
	Optional<BlockTemplate> pull(int height);

	// Returns all blockTemplates without removing any.
	List<BlockTemplate> peekBlockTemplates();
}