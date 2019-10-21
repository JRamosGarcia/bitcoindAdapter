package com.mempoolexplorer.bitcoind.adapter.entities.mempool;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;

public class InMemoryTxPoolImp implements TxPool {

	Logger logger = LoggerFactory.getLogger(InMemoryTxPoolImp.class);

	// Datos propios del Mempool
	// Mapa de TxId a Transaction
	private final Map<String, Transaction> txIdToTxMap;

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
	public void apply(TxPoolDiff memPoolDiff) {

		removeTxs(memPoolDiff.getGoneOrMinedMemPool());
		addTxs(memPoolDiff.getNewMemPool());

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

	private void removeTxs(TxPool memPoolToSubstract) {
		memPoolToSubstract.getTxIdSet().stream().forEach(txId -> {
			txIdToTxMap.remove(txId);
		});
	}

	private void addTxs(TxPool memPoolToAdd) {
		memPoolToAdd.getTxIdSet().stream().forEach(txId -> {
			txIdToTxMap.put(txId, memPoolToAdd.getTx(txId));
		});
	}

}
