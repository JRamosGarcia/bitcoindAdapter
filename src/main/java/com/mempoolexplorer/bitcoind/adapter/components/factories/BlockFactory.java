package com.mempoolexplorer.bitcoind.adapter.components.factories;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResultData;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;

public interface BlockFactory {

	Block from(GetBlockResultData blockResultData, boolean connected);

}
