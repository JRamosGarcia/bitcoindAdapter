package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import java.util.Optional;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;
import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplateChanges;

/**
 * Stores newest and last-used BlockTemplate. Also tracks if there are changes
 * between newest and last-used
 * 
 */
public interface BlockTemplateContainer {

	BlockTemplate getNewestBlockTemplate();

	void setNewestBlockTemplate(BlockTemplate blockTemplate);

	/**
	 * If optional is not empty, changes *MUST BE SENT*
	 * @return
	 */
	Optional<BlockTemplateChanges> getChanges();
}