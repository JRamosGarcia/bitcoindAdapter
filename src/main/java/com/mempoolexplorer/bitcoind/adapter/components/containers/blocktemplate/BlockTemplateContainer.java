package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

public interface BlockTemplateContainer {

	BlockTemplate getBlockTemplate();

	void setBlockTemplate(BlockTemplate blockTemplate);

}