package com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResultData;
import com.mempoolexplorer.bitcoind.adapter.utils.SysProps;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class BlockTemplate {

	// Height of the bock to be mined with this template.
	private int height;
	private Map<String, BlockTemplateTx> blockTemplateTxMap = new ConcurrentHashMap<>();

	final BinaryOperator<BlockTemplateTx> txBuilderMergeFunction = (oldTx, newTx) -> {
		log.error("duplicated txId: {}, this shouldn't be happening", newTx.getTxId());
		return oldTx;
	};

	private BlockTemplate() {
	}

	public BlockTemplate(GetBlockTemplateResultData gbtrd) {
		blockTemplateTxMap = gbtrd.getTransactions().stream().map(BlockTemplateTx::new)
				.collect(Collectors.toMap(BlockTemplateTx::getTxId, btTx -> btTx, txBuilderMergeFunction,
						() -> new ConcurrentHashMap<>(SysProps.HM_INITIAL_CAPACITY_FOR_BLOCK)));
		this.height = gbtrd.getHeight();
	}

	public static BlockTemplate empty() {
		return new BlockTemplate();
	}

}
