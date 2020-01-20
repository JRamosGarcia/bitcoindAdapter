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

public class InMemoryTxPoolImp implements TxPool {

	Logger logger = LoggerFactory.getLogger(InMemoryTxPoolImp.class);

	// Datos propios del Mempool
	// Mapa de TxId a Transaction
	private Map<String, Transaction> txIdToTxMap = new ConcurrentHashMap<String, Transaction>();

	// Mapa de AddrId a lista de TxId que la contienen
	// private final Map<String, List<String>> addrIdToTxIdsMap;

	public InMemoryTxPoolImp(ConcurrentHashMap<String, Transaction> txIdToTxMap) {
		// Validate.isTrue(txIdToTxMap instanceof ConcurrentHashMap<?, ?>);

		this.txIdToTxMap = txIdToTxMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mempoolexplorer.adapter.entities.mempool.MemPool#apply(com.
	 * mempoolexplorer.adapter.entities.mempool.MemPoolDiff)
	 */
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
		// return new HashMap<String, Transaction>(txIdToTxMap);
		return txIdToTxMap;
	}

	@Override
	public Integer getSize() {
		return txIdToTxMap.size();
	}

	private void updateTxs(Map<String,TxAncestryChanges> txAncestryChangesMap) {
		txAncestryChangesMap.entrySet().stream().forEach(entry-> {
			Transaction tx = txIdToTxMap.get(entry.getKey());
			//Transactions are not swapped since cpfpChangesPool does not have additional data(i.e. txinputs data)
			updateTx(tx, entry.getValue());
		});
	}

	private void updateTx(Transaction toUpdateTx, TxAncestryChanges txac) {
		toUpdateTx.setTxAncestry(txac.getTxAncestry());
		toUpdateTx.setFees(txac.getFees());
	}

	private void removeTxs(List<String> listToSubstract) {
		listToSubstract.stream().forEach(txId -> {
			txIdToTxMap.remove(txId);
		});
	}

	private void addTxs(List<Transaction> txsListToAdd) {
		txsListToAdd.stream().forEach(tx -> {
			txIdToTxMap.put(tx.getTxId(), tx);
		});
	}

}
