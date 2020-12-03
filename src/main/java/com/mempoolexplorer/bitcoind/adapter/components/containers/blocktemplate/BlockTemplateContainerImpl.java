package com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

import org.springframework.stereotype.Component;

@Component
public class BlockTemplateContainerImpl implements BlockTemplateContainer {

	private Map<Integer, BlockTemplate> btMap = new ConcurrentHashMap<>();

	@Override
	public void push(BlockTemplate blockTemplate) {
		btMap.put(blockTemplate.getHeight(), blockTemplate);
	}

	@Override
	public Optional<BlockTemplate> pull(int height) {
		return Optional.ofNullable(btMap.get(height));
	}

	@Override
	public List<BlockTemplate> peekBlockTemplates() {
		return btMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public void drop(){
		btMap = new ConcurrentHashMap<>();
	}
}
