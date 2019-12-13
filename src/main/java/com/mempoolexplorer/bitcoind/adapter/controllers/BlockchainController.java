package com.mempoolexplorer.bitcoind.adapter.controllers;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.bitcoind.adapter.components.containers.blockchain.changes.LastBlocksContainer;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;

@RestController
@RequestMapping("/blockChain")
public class BlockchainController {

	@Autowired
	private LastBlocksContainer lastBlocksContainer;

	@GetMapping("/lastBlocks")
	public List<Block> getAllLastBlocks() {
		// Return all stored mined blocks
		return lastBlocksContainer.getLastBlocksFrom(Instant.ofEpochMilli(1));
	}

	@GetMapping("/lastBlocksFrom/{epochSecond}/{nano}")
	public List<Block> getLastBlocksFrom(@PathVariable("epochSecond") Integer epochSecond,
			@PathVariable("nano") Integer nano) {
		Instant instant = Instant.ofEpochSecond(epochSecond, nano);
		return lastBlocksContainer.getLastBlocksFrom(instant);
	}

	@GetMapping("/lastBlocksFrom/{height}")
	public List<Block> getLastBlocksFrom(@PathVariable("height") Integer height) {
		return lastBlocksContainer.getLastBlocksFrom(height);
	}

}
