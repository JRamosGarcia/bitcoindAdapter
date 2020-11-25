package com.mempoolexplorer.bitcoind.adapter.entities.mempool;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxAncestryChanges;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxPoolImp implements TxPool {

	Logger logger = LoggerFactory.getLogger(TxPoolImp.class);

	private ConcurrentHashMap<String, Transaction> txIdToTxMap = new ConcurrentHashMap<>();
	private int mempoolSequence = 0;

	public TxPoolImp() {
	}

	public TxPoolImp(ConcurrentHashMap<String, Transaction> txIdToTxMap) {
		this.txIdToTxMap = txIdToTxMap;
	}

	public TxPoolImp(ConcurrentHashMap<String, Transaction> txIdToTxMap, int mempoolSequence) {
		this.txIdToTxMap = txIdToTxMap;
		this.mempoolSequence = mempoolSequence;
	}

	@Override
	public void apply(TxPoolChanges txPoolChanges, int mempoolSequence) {
		apply(txPoolChanges);
		this.mempoolSequence = mempoolSequence;
	}

	@Override
	public void apply(TxPoolChanges txPoolChanges) {
		removeTxs(txPoolChanges.getRemovedTxsId());
		addTxs(txPoolChanges.getNewTxs());
		updateTxs(txPoolChanges.getTxAncestryChangesMap());
	}

	@Override
	public void apply(int mempoolSequence) {
		this.mempoolSequence = mempoolSequence;
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
	public int getSize() {
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
		txIdToTxMap = new ConcurrentHashMap<>();
		mempoolSequence = 0;
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

	@Override
	public int getMempoolSequence() {
		return mempoolSequence;
	}


}
