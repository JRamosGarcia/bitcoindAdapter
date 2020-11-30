package com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.utils.SysProps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TxPoolChanges {
	private List<Transaction> newTxs = new ArrayList<>();
	private List<String> removedTxsId = new ArrayList<>();
	private Map<String, TxAncestryChanges> txAncestryChangesMap = new HashMap<>(SysProps.EXPECTED_MAX_ANCESTRY_CHANGES);

	public boolean hasNoChanges() {
		return (newTxs.isEmpty() && removedTxsId.isEmpty() && txAncestryChangesMap.isEmpty());
	}

	public boolean hasChanges() {
		return !hasNoChanges();
	}
}
