package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

import org.springframework.stereotype.Component;

@Component
public class BlockTemplateContainerImpl implements BlockTemplateContainer {

	private AtomicReference<BlockTemplate> newestBlockTemplateRef = new AtomicReference<>(BlockTemplate.empty());
	private AtomicReference<BlockTemplate> lastUsedBlockTemplateRef = new AtomicReference<>(BlockTemplate.empty());
	private AtomicBoolean areChanges = new AtomicBoolean(false);

	public BlockTemplate getNewestBlockTemplate() {
		return newestBlockTemplateRef.get();
	}

	@Override
	public void setNewestBlockTemplate(BlockTemplate blockTemplate) {
		this.newestBlockTemplateRef.set(blockTemplate);
		this.areChanges.set(true);
	}

	@Override
	public BlockTemplate getLastUsedBlockTemplate() {
		return lastUsedBlockTemplateRef.get();
	}

	@Override
	public void setNewAsLastUsed() {
		this.lastUsedBlockTemplateRef.set(this.newestBlockTemplateRef.get());
		this.areChanges.set(false);
	}

	@Override
	public boolean areChanges() {
		return this.areChanges.get();
	}

}
