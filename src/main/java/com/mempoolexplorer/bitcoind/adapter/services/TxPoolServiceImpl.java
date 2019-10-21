package com.mempoolexplorer.bitcoind.adapter.services;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.InMemoryTxPoolImp;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.repositories.TxPoolRepository;

@Service
public class TxPoolServiceImpl implements TxPoolService {
	@Autowired
	private TxPoolRepository txPoolRepository;

	Logger logger = LoggerFactory.getLogger(TxPoolServiceImpl.class);

	final BinaryOperator<Transaction> mergeFunction = (oldTx, newTx) -> {
		logger.error("duplicated txId: {}, this shouldn't be happening", newTx.getTxId());
		return oldTx;
	};

	@Override
	public Optional<TxPoolDiff> loadTxPoolFromDB() {

		List<Transaction> txs = txPoolRepository.findAll();

		logger.info(txs.size() + " transactions loaded from DB");

		if (logger.isTraceEnabled()) {
			txs.stream().forEach(tx -> {
				logger.trace("txId: " + tx.getTxId() + " loaded from DB.");
			});
		}
		if (txs.isEmpty()) {
			return Optional.empty();
		}

		ConcurrentHashMap<String, Transaction> txIdToTxMap = txs.stream().collect(
				Collectors.toMap(Transaction::getTxId, tx -> tx, mergeFunction, () -> new ConcurrentHashMap<>()));

		TxPool newMemPool = new InMemoryTxPoolImp(txIdToTxMap);

		TxPool goneOrMinedMemPool = new InMemoryTxPoolImp(new ConcurrentHashMap<>());

		TxPoolDiff diff = new TxPoolDiff(goneOrMinedMemPool, newMemPool);

		return Optional.of(diff);
	}

	@Override
	public void saveAllMemPool(TxPool memPool) {
		txPoolRepository.saveAll(memPool.getFullTxPool().values());
		logger.info("{} transactions saved", memPool.getSize());
	}

	@Override
	public void apply(TxPoolDiff diff) {
		txPoolRepository.saveAll(diff.getNewMemPool().getFullTxPool().values());
		txPoolRepository.deleteAll(diff.getGoneOrMinedMemPool().getFullTxPool().values());
		logger.info("Saving memPoolDiff to DB: {} new, {} removed", diff.getNewMemPool().getSize(),
				diff.getGoneOrMinedMemPool().getSize());

	}

}
