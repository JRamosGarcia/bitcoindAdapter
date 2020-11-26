package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.time.Clock;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResultData;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;

@Component
public class BlockFactoryImpl implements BlockFactory {

	@Autowired
	private Clock clock;

	@Override
	public Block from(GetBlockResultData blockResultData, boolean connected) {
		Block block = new Block();
		block.setConnected(connected);
		block.setChangeTime(Instant.now(clock));
		block.setHash(blockResultData.getHash());
		block.setHeight(blockResultData.getHeight());
		block.setWeight(blockResultData.getWeight());
		block.setMinedTime(Instant.ofEpochSecond(blockResultData.getTime()));
		block.setMedianMinedTime(Instant.ofEpochSecond(blockResultData.getMediantime()));
		block.setTxIds(blockResultData.getTx());

		return block;
	}

}
