package com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResultData;
import com.mempoolexplorer.bitcoind.adapter.utils.SysProps;

public class BlockTemplate {

	private static Logger logger = LoggerFactory.getLogger(BlockTemplate.class);

	private Map<String, BlockTemplateTx> blockTemplateTxMap = new ConcurrentHashMap<>();

	final BinaryOperator<BlockTemplateTx> txBuilderMergeFunction = (oldTx, newTx) -> {
		logger.error("duplicated txId: {}, this shouldn't be happening", newTx.getTxId());
		return oldTx;
	};

	private BlockTemplate() {
	}

	public BlockTemplate(GetBlockTemplateResultData gbtrd) {
		blockTemplateTxMap = gbtrd.getTransactions().stream().map(BlockTemplateTx::new)
				.collect(Collectors.toMap(BlockTemplateTx::getTxId, btTx -> btTx, txBuilderMergeFunction,
						() -> new ConcurrentHashMap<>(SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK)));
	}

	public static BlockTemplate empty() {
		return new BlockTemplate();
	}

	public Map<String, BlockTemplateTx> getBlockTemplateTxMap() {
		return blockTemplateTxMap;
	}

	public void setBlockTemplateTxMap(Map<String, BlockTemplateTx> blockTemplateTxMap) {
		this.blockTemplateTxMap = blockTemplateTxMap;
	}

}
