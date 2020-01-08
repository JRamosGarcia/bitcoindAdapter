package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolVerboseData;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionOutput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionResult;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.components.factories.utils.TransactionFactory;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.TxInput;
import com.mempoolexplorer.bitcoind.adapter.entities.TxOutput;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.InMemoryTxPoolImp;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.metrics.ProfileMetricNames;
import com.mempoolexplorer.bitcoind.adapter.metrics.annotations.ProfileTime;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;

@Component
public class InMemTxPoolFillerImpl implements TxPoolFiller {

	Logger logger = LoggerFactory.getLogger(InMemTxPoolFillerImpl.class);

	@Autowired
	private BitcoindClient bitcoindClient;

	final BinaryOperator<Transaction> mergeFunction = (oldTx, newTx) -> {
		logger.error("duplicated txId: {}, this shouldn't be happening", newTx.getTxId());
		return oldTx;
	};

	final BinaryOperator<Transaction> txBuilderMergeFunction = (oldTx, newTx) -> {
		logger.error("duplicated txId: {}, this shouldn't be happening", newTx.getTxId());
		return oldTx;
	};
	// getDepends is the content we use because it's the most distinguishable
	final BinaryOperator<GetRawMemPoolVerboseData> rawMemPoolVerboseDataMergeFuncion = (oldTx, newTx) -> {
		logger.error("duplicated txId: {}, this shouldn't be happening", newTx.getDepends());
		return oldTx;
	};

	/**
	 * Mempool creation involves calling getRawMemPoolVerbose and adding additional
	 * data through getVerboseRawTransaction. Finally, addresses and amounts for
	 * TxInput are queried using getVerboseRawTransaction again. bitcoind node must
	 * be started with txindex=1 flag.
	 */
	@Override
	@ProfileTime(metricName = ProfileMetricNames.MEMPOOL_INITIAL_CREATION_TIME)
	public TxPool createMemPool() throws TxPoolException {
		try {
			GetRawMemPoolVerbose rawMemPoolVerbose = bitcoindClient.getRawMemPoolVerbose();

			if (null != rawMemPoolVerbose.getError()) {
				// We can't recover from this error.
				throw new TxPoolException("Error, getRawMemPoolVerbose returned error: " + rawMemPoolVerbose.getError()
						+ ", requestId: " + rawMemPoolVerbose.getId());
			}

			logger.debug("creating initial mempool bitcoindClient.getRawMemPoolVerbose() returned a mempool with {}",
					rawMemPoolVerbose.getGetRawMemPoolVerboseDataMap().size());
			ConcurrentHashMap<String, Transaction> txIdToTxMap = createTxIdToTxMapFrom(
					rawMemPoolVerbose.getGetRawMemPoolVerboseDataMap());
			addAdditionalData(txIdToTxMap);

			return new InMemoryTxPoolImp(txIdToTxMap);
		} catch (ResourceAccessException e) {
			throw new TxPoolException("Error: Can't connect to bitcoindClient", e);
		}
	}

	@Override
	@ProfileTime(metricName = ProfileMetricNames.MEMPOOL_REFRESH_TIME)
	public TxPoolDiff obtainMemPoolDiffs(TxPool txPool) throws TxPoolException {
		try {
			Set<String> myMemPoolKeySet = txPool.getFullTxPool().keySet();

			// TODO: Aqui deberíamos usar getmempool sin verbose y luego preguntar por los
			// que faltan, si no esto va a tardar mucho cuando el mempool sea grande
			GetRawMemPoolVerbose rawMemPoolVerbose = bitcoindClient.getRawMemPoolVerbose();
			logger.debug(
					"refreshing mempool bitcoindClient.getRawMemPoolVerbose() returned a mempool with {} transactions",
					rawMemPoolVerbose.getGetRawMemPoolVerboseDataMap().size());

			if (null != rawMemPoolVerbose.getError()) {
				// We can't recover from this error.
				throw new TxPoolException("Error, getRawMemPoolVerbose returned error: " + rawMemPoolVerbose.getError()
						+ ", requestId: " + rawMemPoolVerbose.getId());
			}

			Set<String> inNetMemPoolKeySet = rawMemPoolVerbose.getGetRawMemPoolVerboseDataMap().keySet();

			// Mined or deleted transactions are obtained by substracting on-net
			// transactions to our current transaction set. Normally a minedBlock.
			ConcurrentHashMap<String, Transaction> goneOrMinedTxIdToTxMap = txPool.getFullTxPool().entrySet().stream()
					.filter(entry -> !inNetMemPoolKeySet.contains(entry.getKey())).collect(Collectors
							.toMap(Entry::getKey, Entry::getValue, mergeFunction, () -> new ConcurrentHashMap<>()));

			InMemoryTxPoolImp goneOrMinedMemPool = new InMemoryTxPoolImp(
					goneOrMinedTxIdToTxMap/* , addrIdToTxIdsMap */);

			logger.debug("There are {} transactions mined or gone", goneOrMinedMemPool.getFullTxPool().size());

			// New transactions are obtained substracting our transaction set to on-net
			// transactions.
			Map<String, GetRawMemPoolVerboseData> newRawMemPoolVerboseDataMap = rawMemPoolVerbose
					.getGetRawMemPoolVerboseDataMap().entrySet().stream()
					.filter(entry -> !myMemPoolKeySet.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey,
							Entry::getValue, rawMemPoolVerboseDataMergeFuncion, () -> new HashMap<>()));

			// Add additional data (addresses and amounts) to new transactions and create
			// addr->List<TxIds> map.
			ConcurrentHashMap<String, Transaction> newTxIdToTxMap = createTxIdToTxMapFrom(newRawMemPoolVerboseDataMap);
			addAdditionalData(newTxIdToTxMap);

			InMemoryTxPoolImp newMemPool = new InMemoryTxPoolImp(newTxIdToTxMap/* , addrIdToTxIdsMap */);

			logger.debug("There are {} new transactions in memPool", newMemPool.getFullTxPool().size());

			return new TxPoolDiff(goneOrMinedMemPool, newMemPool);
		} catch (ResourceAccessException e) {
			throw new TxPoolException("Error: Can't connect to bitcoindClient", e);
		}
	}

	private ConcurrentHashMap<String, Transaction> createTxIdToTxMapFrom(
			Map<String, GetRawMemPoolVerboseData> rawMemPoolVerboseDataMap) {

		ConcurrentHashMap<String, Transaction> txIdToTxMap = rawMemPoolVerboseDataMap.entrySet().stream()
				.map((entry) -> TransactionFactory.from(entry.getKey(), entry.getValue())).collect(Collectors.toMap(
						Transaction::getTxId, tx -> tx, txBuilderMergeFunction, () -> new ConcurrentHashMap<>()));

		return txIdToTxMap;
	}

	/**
	 * Adds addresses, amount Data and hex raw data to each transaction in the
	 * transacion map.
	 * @param txIdToTxMap
	 */
	private void addAdditionalData(Map<String, Transaction> txIdToTxMap) {

		// If a mempool transaction is dumped beetween bitcoind RPC calls, the
		// transaction is removed from map.
		Set<String> withErrorTxIdSet = new HashSet<>();

		final AtomicInteger count = new AtomicInteger();
		//TODO: Estudiar meter un parallel aqui.
		txIdToTxMap.entrySet().stream().forEach(entry -> {
			GetVerboseRawTransactionResult rawTx = bitcoindClient.getVerboseRawTransaction(entry.getKey());

			int i = count.incrementAndGet();
			logger.debug("Obtained tx number: {} rawTx: {}", i, entry.getKey());

			if (null == rawTx.getError()) {
				// Add more data via getRawTransaction, be aware more txIds can be added to
				// withErrorTxIdSet
				addAdditionalData(rawTx, entry.getValue(), withErrorTxIdSet);
			} else {
				String withErrorTxId = entry.getKey();
				withErrorTxIdSet.add(entry.getKey());
				logWarnOnTransactionWithError(rawTx, withErrorTxId);
			}
		});
		// Delete all transactions which all data could't be obtained.
		String log = new String();
		if (!withErrorTxIdSet.isEmpty()) {
			log = "txIds deleted for bitcoind RPC race conditions: ";
			log += withErrorTxIdSet.stream().collect(Collectors.joining(" ,", "[", "]"));
			logger.warn(log);// This can happen exceptionally, we track it.
		}
		withErrorTxIdSet.stream().forEach(key -> txIdToTxMap.remove(key));
	}

	private void validateTx(Transaction tx) {
		Validate.notNull(tx.getTxId(), "txId can't be null");
		Validate.notNull(tx.getTxInputs(), "txInputs can't be null");
		Validate.notNull(tx.getTxOutputs(), "txOutputs can't be null");
		Validate.notNull(tx.getSize(), "size can't be null");
		Validate.notNull(tx.getvSize(), "vsize can't be null");
		Validate.notNull(tx.getSatBytes(), "satBytes can't be null");
		Validate.notNull(tx.getDescendantCount(), "descendantCount can't be null");
		Validate.notNull(tx.getDescendantSize(), "descendantSize can't be null");
		Validate.notNull(tx.getAncestorCount(), "ancestorCount can't be null");
		Validate.notNull(tx.getAncestorSize(), "ancestorSize can't be null");
		Validate.notNull(tx.getDepends(), "depends can't be null");
		Validate.notNull(tx.getFees(), "Fees object can't be null");
		Validate.notNull(tx.getFees().getBase(), "Fees.base can't be null");
		Validate.notNull(tx.getFees().getModified(), "Fees.modified can't be null");
		Validate.notNull(tx.getFees().getAncestor(), "Fees.ancestor can't be null");
		Validate.notNull(tx.getFees().getDescendant(), "Fees.descendant can't be null");

		Validate.notEmpty(tx.getTxInputs(), "txInputs can't be empty");
		Validate.notEmpty(tx.getTxOutputs(), "txOutputs can't be empty");

		// tx.getTxInputs // We don't validate for not nulls since all fields can be
		// null if it's a coinbase transaction.

		tx.getTxOutputs().forEach(output -> {
			// addressIds can be null if script is not recognized.
			Validate.notNull(output.getAmount(), "amount can't be null in a TxOutput");
			Validate.notNull(output.getIndex(), "index can't be null in a TxOutput");
		});

	}

	/**
	 * Add more data via getRawTransaction (addresses, amount data and raw Tx in hexadecimal format)
	 * @param rawTx rawTransaction obtained from getRawTransaction RPC
	 * @param tx transaction to be filled
	 * @param withErrorTxIdSet tx deleted for bitcoind RPC race conditions
	 */
	private void addAdditionalData(GetVerboseRawTransactionResult rawTx, Transaction tx,
			Set<String> withErrorTxIdSet) {

		// Add raw tx in hex
		tx.setHex(rawTx.getGetRawTransactionResultData().getHex());
		
		// Add Txoutputs to transaccions
		rawTx.getGetRawTransactionResultData().getVout().stream().forEach(output -> {
			// JSON preserves order. http://www.rfc-editor.org/rfc/rfc7159.txt
			TxOutput txOutput = new TxOutput();
			txOutput.setAddressIds(output.getScriptPubKey().getAddresses());
			txOutput.setAmount(JSONUtils.JSONtoAmount(output.getValue()));
			txOutput.setIndex(output.getN());
			tx.getTxOutputs().add(txOutput);
		});

		// Add Txinputs to transaccions
		rawTx.getGetRawTransactionResultData().getVin().stream().forEach(input -> {

			// obtain transaction output which is spent in current transaction input.
			GetVerboseRawTransactionResult spentRawTx = bitcoindClient.getVerboseRawTransaction(input.getTxid());
			if (null != spentRawTx.getError()) {
				String withErrorTxId = rawTx.getGetRawTransactionResultData().getTxid();
				withErrorTxIdSet.add(withErrorTxId);
				logWarnOnTransactionWithError(rawTx, withErrorTxId);
			} else {
				// JSON preserves order. http://www.rfc-editor.org/rfc/rfc7159.txt
				GetVerboseRawTransactionOutput spentTxOutput = spentRawTx.getGetRawTransactionResultData().getVout()
						.get(input.getVout());

				TxInput txInput = new TxInput();
				txInput.setAddressIds(spentTxOutput.getScriptPubKey().getAddresses());
				txInput.setAmount(JSONUtils.JSONtoAmount(spentTxOutput.getValue()));
				txInput.setTxId(input.getTxid());
				txInput.setvOutIndex(input.getVout());

				// No need to sort data here.
				tx.getTxInputs().add(txInput);
			}
		});
		// Satoshis per byte are calulated here. (Segwit compatible)
		int vSize = rawTx.getGetRawTransactionResultData().getVsize();
		tx.setvSize(vSize);
		tx.setSatBytes(((double) tx.getFees().getBase()) / ((double) vSize));
		// At this point transaction must be correct if not error, we validate it.
		if ((null != tx.getTxId()) && (!withErrorTxIdSet.contains(tx.getTxId()))) {
			validateTx(tx);
		}
	}

	// This can happen exceptionally, we track it.
	private void logWarnOnTransactionWithError(GetVerboseRawTransactionResult rawTx, String incompleteTxId) {
		String error = rawTx == null ? "GetVerboseRawTransactionResult is null"
				: (rawTx.getError() == null ? "GetVerboseRawTransactionResult is null" : rawTx.getError().toString());
		logger.warn("Error retrieving txId: " + incompleteTxId + " error: " + error);
	}
}
