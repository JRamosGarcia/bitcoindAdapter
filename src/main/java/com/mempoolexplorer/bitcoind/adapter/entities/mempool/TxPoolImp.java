package com.mempoolexplorer.bitcoind.adapter.entities.mempool;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxAncestryChanges;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

public class TxPoolImp implements TxPool {

	Logger logger = LoggerFactory.getLogger(TxPoolImp.class);

	private ConcurrentHashMap<String, Transaction> txIdToTxMap = new ConcurrentHashMap<>();

	public TxPoolImp() {
	}

	public TxPoolImp(ConcurrentHashMap<String, Transaction> txIdToTxMap) {

		this.txIdToTxMap = txIdToTxMap;
	}

	@Override
	public void apply(TxPoolChanges txPoolChanges) {

		removeTxs(txPoolChanges.getRemovedTxsId());
		addTxs(txPoolChanges.getNewTxs());
		updateTxs(txPoolChanges.getTxAncestryChangesMap());
	}

	@Override
	public Set<String> getTxIdSet() {
		return txIdToTxMap.keySet();
	}

	@Override
	public Transaction getTx(String txId) {
		return txIdToTxMap.get(txId);
	}

	@Override
	public Map<String, Transaction> getFullTxPool() {
		return txIdToTxMap;
	}

	@Override
	public Integer getSize() {
		return txIdToTxMap.size();
	}

	private void updateTxs(Map<String, TxAncestryChanges> txAncestryChangesMap) {
		txAncestryChangesMap.entrySet().stream().forEach(entry -> {
			Transaction tx = txIdToTxMap.get(entry.getKey());
			// Transactions are not swapped since cpfpChangesPool does not have additional
			// data(i.e. txinputs data)
			updateTx(tx, entry.getValue());
		});
	}

	@Override
	public void drop() {
		txIdToTxMap.clear();
	}

	private void updateTx(Transaction toUpdateTx, TxAncestryChanges txac) {
		toUpdateTx.setTxAncestry(txac.getTxAncestry());
		toUpdateTx.setFees(txac.getFees());
	}

	private void removeTxs(List<String> listToSubstract) {
		listToSubstract.stream().forEach(txId -> txIdToTxMap.remove(txId));
	}

	private void addTxs(List<Transaction> txsListToAdd) {
		txsListToAdd.stream().forEach(tx -> txIdToTxMap.put(tx.getTxId(), tx));
	}

}
