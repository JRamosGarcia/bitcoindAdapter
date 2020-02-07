package com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate;

import java.util.List;
import java.util.stream.Collectors;

public class BlockTemplateChanges {

	private List<BlockTemplateTx> addBTTxsList;
	private List<String> removeBTTxIdsList;

	public BlockTemplateChanges(BlockTemplate newBT, BlockTemplate oldBT) {
		addBTTxsList = newBT.getBlockTemplateTxMap().values().stream()
				.filter(btTx -> !oldBT.getBlockTemplateTxMap().containsKey(btTx.getTxId()))
				.collect(Collectors.toList());
		removeBTTxIdsList = oldBT.getBlockTemplateTxMap().keySet().stream()
				.filter(btTxId -> !newBT.getBlockTemplateTxMap().containsKey(btTxId)).collect(Collectors.toList());

	}

	public List<BlockTemplateTx> getAddBTTxsList() {
		return addBTTxsList;
	}

	public List<String> getRemoveBTTxIdsList() {
		return removeBTTxIdsList;
	}

}
