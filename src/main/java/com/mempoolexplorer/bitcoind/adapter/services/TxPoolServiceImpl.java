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
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
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
	public Optional<TxPool> loadTxPoolFromDB() {

		List<Transaction> txs = txPoolRepository.findAll();

		logger.info(txs.size() + " transactions loaded from DB");

		if (logger.isTraceEnabled()) {
			txs.stream().forEach(tx -> {
				logger.trace("txId: " + tx.getTxId() + " loaded from DB.");
			});
		}

		if (txs.size() == 0) {
			return Optional.empty();
		}

		return Optional.of(new InMemoryTxPoolImp(txs.stream().collect(
				Collectors.toMap(Transaction::getTxId, tx -> tx, mergeFunction, () -> new ConcurrentHashMap<>()))));

	}

	@Override
	public void saveAllMemPool(TxPool memPool) {
		txPoolRepository.saveAll(memPool.getFullTxPool().values());
		logger.info("{} transactions saved", memPool.getSize());
	}

	@Override
	public void apply(TxPoolChanges txpc) {
		txPoolRepository.saveAll(txpc.getNewTxs());

		txpc.getRemovedTxsId().stream().forEach(txId -> {
			Optional<Transaction> opTx = txPoolRepository.findById(txId);
			if (opTx.isPresent()) {
				txPoolRepository.delete(opTx.get());
			} else {
				logger.info("Non existent transaction in db: {}", txId);
			}
		});

		txpc.getTxAncestryChangesMap().entrySet().forEach(entry -> {
			Optional<Transaction> bdTxOpt = txPoolRepository.findById(entry.getKey());
			if (bdTxOpt.isPresent()) {
				Transaction bdTx = bdTxOpt.get();
				bdTx.setTxAncestry(entry.getValue().getTxAncestry());
				bdTx.setFees(entry.getValue().getFees());
				txPoolRepository.save(bdTx);
			} else {
				logger.info("Non existent transaction in db: {}", entry.getKey());
			}
		});
		logger.info("Saving memPoolDiff to DB: {} new, {} removed {} updated", txpc.getNewTxs().size(),
				txpc.getRemovedTxsId().size(), txpc.getTxAncestryChangesMap().size());
	}
}
