package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

/**
 * Stores newest and last-used BlockTemplate. Also tracks if there are changes
 * between newest and last-used
 * 
 */
public interface BlockTemplateContainer {

	BlockTemplate getNewestBlockTemplate();

	void setNewestBlockTemplate(BlockTemplate blockTemplate);

	BlockTemplate getLastUsedBlockTemplate();

	void setNewAsLastUsed();

	boolean areChanges();
}