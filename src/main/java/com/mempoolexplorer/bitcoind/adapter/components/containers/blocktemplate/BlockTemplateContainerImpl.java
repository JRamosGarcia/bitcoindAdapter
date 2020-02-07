package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

@Component
public class BlockTemplateContainerImpl implements BlockTemplateContainer {

	private AtomicReference<BlockTemplate> blockTemplateRef = new AtomicReference<>(BlockTemplate.empty());

	@Override
	public BlockTemplate getBlockTemplate() {
		return blockTemplateRef.get();
	}

	@Override
	public void setBlockTemplate(BlockTemplate blockTemplate) {
		this.blockTemplateRef.set(blockTemplate);
	}

}
