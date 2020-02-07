package com.mempoolexplorer.bitcoind.adapter.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

@RestController
@RequestMapping("/blockTemplate")
public class BlockTemplateController {

	@Autowired
	private BlockTemplateContainer blockTemplateContainer;

	@GetMapping("/blockTemplate")
	public BlockTemplate getBlockTemplate() {
		return blockTemplateContainer.getBlockTemplate();
	}

}
