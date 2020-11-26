package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;
import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplateChanges;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BlockTemplateContainerImpl implements BlockTemplateContainer {

	private AtomicReference<BlockTemplate> newestBlockTemplateRef = new AtomicReference<>(BlockTemplate.empty());
	private AtomicReference<BlockTemplate> lastUsedBlockTemplateRef = new AtomicReference<>(BlockTemplate.empty());
	private AtomicBoolean areChanges = new AtomicBoolean(false);

	/**
	 * @deprecated must be deleted when long poolling removed.
	 */
	@Deprecated(since = "0.2")
	@Override
	public BlockTemplate getNewestBlockTemplate() {
		return newestBlockTemplateRef.get();
	}

	@Override
	public void setNewestBlockTemplate(BlockTemplate blockTemplate) {
		this.newestBlockTemplateRef.set(blockTemplate);
		this.areChanges.set(true);
	}

	private void setNewAsLastUsed() {
		this.lastUsedBlockTemplateRef.set(this.newestBlockTemplateRef.get());
		this.areChanges.set(false);
	}

	private boolean areChanges() {
		return this.areChanges.get();
	}

	@Override
	public Optional<BlockTemplateChanges> getChanges() {
		if (!areChanges()) {
			return Optional.empty();
		}
		BlockTemplateChanges blockTemplateChanges = new BlockTemplateChanges(newestBlockTemplateRef.get(),
				lastUsedBlockTemplateRef.get());
		setNewAsLastUsed();
		log.info("new BlockTemplate(size: {} new: {} remove: {})",
				newestBlockTemplateRef.get().getBlockTemplateTxMap().size(),
				blockTemplateChanges.getAddBTTxsList().size(), blockTemplateChanges.getRemoveBTTxIdsList().size());
		return Optional.of(blockTemplateChanges);
	}
}
