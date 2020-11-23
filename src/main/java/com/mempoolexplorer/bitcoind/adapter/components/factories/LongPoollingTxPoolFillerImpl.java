package com.mempoolexplorer.bitcoind.adapter.components.factories;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.BitcoindError;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetMemPoolInfo;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetRawMemPoolVerbose;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionOutput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.RawMemPoolEntryData;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.components.factories.utils.TransactionFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.utils.TxAncestryChangesFactory;
import com.mempoolexplorer.bitcoind.adapter.entities.Fees;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.TxAncestry;
import com.mempoolexplorer.bitcoind.adapter.entities.TxInput;
import com.mempoolexplorer.bitcoind.adapter.entities.TxOutput;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolImp;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.metrics.ProfileMetricNames;
import com.mempoolexplorer.bitcoind.adapter.metrics.annotations.ProfileTime;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;
import com.mempoolexplorer.bitcoind.adapter.utils.PercentLog;
import com.mempoolexplorer.bitcoind.adapter.utils.SysProps;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

@Component
public class LongPoollingTxPoolFillerImpl implements LongPoollingTxPoolFiller {

	Logger logger = LoggerFactory.getLogger(LongPoollingTxPoolFillerImpl.class);

	@Autowired
	private Clock clock;

	private AtomicInteger changeCounter = new AtomicInteger(0);

	@Autowired
	private BitcoindClient bitcoindClient;

	String err = "duplicated txId: {}, this shouldn't be happening";

	final BinaryOperator<Transaction> mergeFunction = (oldTx, newTx) -> {
		logger.error(err, newTx.getTxId());
		return oldTx;
	};

	final BinaryOperator<Transaction> txBuilderMergeFunction = (oldTx, newTx) -> {
		logger.error(err, newTx.getTxId());
		return oldTx;
	};
	// getDepends is the content we use because it's the most distinguishable
	final BinaryOperator<RawMemPoolEntryData> rawMemPoolVerboseDataMergeFuncion = (oldTx, newTx) -> {
		logger.error(err, newTx.getDepends());
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
			GetMemPoolInfo memPoolInfo = bitcoindClient.getMemPoolInfo();
			logger.info("Bitcoind node has {} tx in his mempoool, loading...",
					memPoolInfo.getGetMemPoolInfoData().getSize());
			GetRawMemPoolVerbose rawMemPoolVerbose = bitcoindClient.getRawMemPoolVerbose();

			if (null != rawMemPoolVerbose.getError()) {
				// We can't recover from this error.
				throw new TxPoolException("Error, getRawMemPoolVerbose returned error: " + rawMemPoolVerbose.getError()
						+ ", requestId: " + rawMemPoolVerbose.getId());
			}

			logger.debug("creating initial mempool bitcoindClient.getRawMemPoolVerbose() returned a mempool with {}",
					rawMemPoolVerbose.getRawMemPoolEntryDataMap().size());
			ConcurrentHashMap<String, Transaction> txIdToTxMap = createTxIdToTxMapFrom(
					rawMemPoolVerbose.getRawMemPoolEntryDataMap());
			addAdditionalData(txIdToTxMap, true);

			return new TxPoolImp(txIdToTxMap);
		} catch (ResourceAccessException e) {
			throw new TxPoolException("Error: Can't connect to bitcoindClient", e);
		}
	}

	@Override
	@ProfileTime(metricName = ProfileMetricNames.MEMPOOL_REFRESH_TIME)
	public TxPoolChanges obtainMemPoolChanges(TxPool txPool) throws TxPoolException {
		Instant start = Instant.now();
		TxPoolChanges txpc = obtainMemPoolChangesNoProfilling(txPool);
		Instant end = Instant.now();
		Duration duration = Duration.between(start, end);
		logger.info("Diff ha tardado: {}", duration);
		return txpc;
	}

	private TxPoolChanges obtainMemPoolChangesNoProfilling(TxPool txPool) throws TxPoolException {
		try {
			TxPoolChanges txpc = new TxPoolChanges();
			txpc.setChangeTime(Instant.now(clock));
			txpc.setChangeCounter(changeCounter.addAndGet(1));
			Set<String> myMemPoolKeySet = txPool.getFullTxPool().keySet();
			GetRawMemPoolVerbose rawMemPoolVerbose = bitcoindClient.getRawMemPoolVerbose();

			logIfErrors("getRawMemPoolVerbose", rawMemPoolVerbose.getRawMemPoolEntryDataMap().size(),
					rawMemPoolVerbose.getError());

			// goneOrMined and cpfpUpdate are calculated in only one iteration
			calculateGoneOrMinedANDCpfpChanges(txPool, rawMemPoolVerbose.getRawMemPoolEntryDataMap(), txpc);

			// New transactions are obtained substracting our transaction set to on-net
			// transactions. We need a map for substracting tx with errors
			Map<String, Transaction> newRawMemPoolVerboseDataMap = rawMemPoolVerbose.getRawMemPoolEntryDataMap()
					.entrySet().stream().filter(entry -> !myMemPoolKeySet.contains(entry.getKey()))
					.map(entry -> TransactionFactory.from(entry.getKey(), entry.getValue())).collect(Collectors.toMap(
							tx -> tx.getTxId(), tx -> tx, txBuilderMergeFunction, () -> new ConcurrentHashMap<>()));

			addAdditionalData(newRawMemPoolVerboseDataMap, false);

			txpc.setNewTxs(newRawMemPoolVerboseDataMap.values().stream().collect(Collectors.toList()));
			logger.debug("There are {} new transactions in memPool", txpc.getNewTxs().size());

			return txpc;
		} catch (ResourceAccessException e) {
			throw new TxPoolException("Error: Can't connect to bitcoindClient", e);
		}
	}

	/**
	 * Calculates goneOrMinedMemPool and cpfpUpdateMemPool
	 * 
	 * @param txPool                 actual mempool
	 * @param rawMemPoolEntryDataMap mempool from bitcoind
	 * @return a list with goneOrMinedMemPool and cpfpUpdateMemPool
	 */
	private void calculateGoneOrMinedANDCpfpChanges(TxPool txPool,
			Map<String, RawMemPoolEntryData> rawMemPoolEntryDataMap, TxPoolChanges txpc) {

		txPool.getFullTxPool().entrySet().stream().forEach(entry -> {
			RawMemPoolEntryData rawMemPoolEntryData = rawMemPoolEntryDataMap.get(entry.getKey());
			if (null == rawMemPoolEntryData) {
				txpc.getRemovedTxsId().add(entry.getKey());
			} else {// nulls are not updated because are going to be deleted.
				if (cpfpHasChanged(entry.getValue(), rawMemPoolEntryData)) {
					txpc.getTxAncestryChangesMap().put(entry.getKey(),
							TxAncestryChangesFactory.from(rawMemPoolEntryData));
				}
			}
		});
		logger.debug("There are {} transactions mined or gone", txpc.getRemovedTxsId().size());
		logger.debug("There are {} transactions updated for cpfp", txpc.getTxAncestryChangesMap().size());
	}

	private boolean cpfpHasChanged(Transaction tx, RawMemPoolEntryData rawMemPoolEntryData) {
		TxAncestry txa = tx.getTxAncestry();
		Fees txf = tx.getFees();
		if ((!txa.getDescendantCount().equals(rawMemPoolEntryData.getDescendantcount()))
				|| (!txa.getDescendantSize().equals(rawMemPoolEntryData.getDescendantsize()))
				|| (!txa.getAncestorCount().equals(rawMemPoolEntryData.getAncestorcount()))
				|| (!txa.getAncestorSize().equals(rawMemPoolEntryData.getAncestorsize()))
				|| (!txa.getDepends().equals(rawMemPoolEntryData.getDepends()))
				|| (!txa.getSpentby().equals(rawMemPoolEntryData.getSpentby()))
				|| (!txf.getAncestor().equals(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getAncestor())))
				|| (!txf.getDescendant().equals(JSONUtils.jsonToAmount(rawMemPoolEntryData.getFees().getDescendant())))

		) {
			return true;
		}
		return false;
	}

	private void logIfErrors(String methodCalled, int memPoolSize, BitcoindError error) throws TxPoolException {
		logger.debug("refreshing mempool bitcoindClient.{}() returned a mempool with {} transactions", methodCalled,
				memPoolSize);

		if (null != error) {
			// We can't recover from this error.
			throw new TxPoolException("Error, getRawMemPoolVerbose returned error: " + error);
		}
	}

	private ConcurrentHashMap<String, Transaction> createTxIdToTxMapFrom(
			Map<String, RawMemPoolEntryData> rawMemPoolVerboseDataMap) {

		return rawMemPoolVerboseDataMap.entrySet().stream()
				.map((entry) -> TransactionFactory.from(entry.getKey(), entry.getValue()))
				.collect(Collectors.toMap(Transaction::getTxId, tx -> tx, txBuilderMergeFunction,
						() -> new ConcurrentHashMap<>(SysProps.EXPECTED_MEMPOOL_SIZE)));
	}

	/**
	 * Adds addresses, amount Data and hex raw data to each transaction in the
	 * transacion map.
	 * 
	 * @param txIdToTxMap
	 */
	private void addAdditionalData(Map<String, Transaction> txIdToTxMap, boolean logIt) {

		// If a mempool transaction is dumped beetween bitcoind RPC calls, the
		// transaction is removed from map.
		Set<String> withErrorTxIdSet = new HashSet<>();

		int count = 0;
		PercentLog pl = new PercentLog(txIdToTxMap.size(), 1);

		// There is no gain making this parallel via java 8 parallelStream due to:
		// https://bitcoin.stackexchange.com/questions/89066/how-to-scale-json-rpc
		// Also: I've tested it

		for (Entry<String, Transaction> entry : txIdToTxMap.entrySet()) {

			GetVerboseRawTransactionResult rawTx = bitcoindClient.getVerboseRawTransaction(entry.getKey());

			logger.debug("Obtained tx number: {} rawTx: {}", count, entry.getKey());
			if (logIt) {
				pl.update(count, (percent) -> logger.info("Adding additional data to txs... {}", percent));
			}
			if (null == rawTx.getError()) {
				// Add more data via getRawTransaction, be aware more txIds can be added to
				// withErrorTxIdSet
				addAdditionalData(rawTx, entry.getValue(), withErrorTxIdSet);
			} else {
				String withErrorTxId = entry.getKey();
				withErrorTxIdSet.add(entry.getKey());
				logWarnOnTransactionWithError(rawTx, withErrorTxId);
			}
			count++;
		}

		// Delete all transactions which all data could't be obtained.
		String log = "";
		if (!withErrorTxIdSet.isEmpty()) {
			log = "txIds deleted for bitcoind RPC race conditions: ";
			log += withErrorTxIdSet.stream().collect(Collectors.joining(" ,", "[", "]"));
			logger.warn(log);// This can happen frequently when mempool is "full", we track it.
		}
		withErrorTxIdSet.stream().forEach(key -> txIdToTxMap.remove(key));
	}

	private void validateTx(Transaction tx) {
		Validate.notNull(tx.getTxId(), "txId can't be null");
		Validate.notNull(tx.getTxInputs(), "txInputs can't be null");
		Validate.notNull(tx.getTxOutputs(), "txOutputs can't be null");
		Validate.notNull(tx.getWeight(), "weight can't be null");
		Validate.notNull(tx.getFees(), "Fees object can't be null");
		Validate.notNull(tx.getFees().getBase(), "Fees.base can't be null");
		Validate.notNull(tx.getFees().getModified(), "Fees.modified can't be null");
		Validate.notNull(tx.getFees().getAncestor(), "Fees.ancestor can't be null");
		Validate.notNull(tx.getFees().getDescendant(), "Fees.descendant can't be null");
		Validate.notNull(tx.getTimeInSecs(), "timeInSecs can't be null");
		Validate.notNull(tx.getTxAncestry(), "txAncestry can't be null");
		Validate.notNull(tx.getTxAncestry().getDescendantCount(), "descendantCount can't be null");
		Validate.notNull(tx.getTxAncestry().getDescendantSize(), "descendantSize can't be null");
		Validate.notNull(tx.getTxAncestry().getAncestorCount(), "ancestorCount can't be null");
		Validate.notNull(tx.getTxAncestry().getAncestorSize(), "ancestorSize can't be null");
		Validate.notNull(tx.getTxAncestry().getDepends(), "depends can't be null");
		Validate.notNull(tx.getBip125Replaceable(), "bip125Replaceable can't be null");
		Validate.notEmpty(tx.getHex(), "Hex can't be empty");

		tx.getTxInputs().forEach(input -> {
			if (input.getCoinbase() == null) {
				Validate.notNull(input.getTxId(), "input.txId can't be null");
				Validate.notNull(input.getVOutIndex(), "input.voutIndex can't be null");
				Validate.notNull(input.getAmount(), "input.amount can't be null");
				// Input address could be null in case of unrecognized input scripts
				// Validate.notNull(input.getAddressIds());
			}
		});

		tx.getTxOutputs().forEach(output -> {
			// addressIds can be null if script is not recognized.
			Validate.notNull(output.getAmount(), "amount can't be null in a TxOutput");
			Validate.notNull(output.getIndex(), "index can't be null in a TxOutput");
		});

	}

	/**
	 * Add more data via getRawTransaction (addresses, amount data and raw Tx in
	 * hexadecimal format)
	 * 
	 * @param rawTx            rawTransaction obtained from getRawTransaction RPC
	 * @param tx               transaction to be filled
	 * @param withErrorTxIdSet tx deleted for bitcoind RPC race conditions
	 */
	private void addAdditionalData(GetVerboseRawTransactionResult rawTx, Transaction tx, Set<String> withErrorTxIdSet) {

		tx.setHex(rawTx.getGetRawTransactionResultData().getHex());

		// Add Txoutputs to transaccions
		rawTx.getGetRawTransactionResultData().getVout().stream().forEach(output -> {
			// JSON preserves order. http://www.rfc-editor.org/rfc/rfc7159.txt
			TxOutput txOutput = new TxOutput();
			txOutput.setAddressIds(output.getScriptPubKey().getAddresses());
			txOutput.setAmount(JSONUtils.jsonToAmount(output.getValue()));
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
				txInput.setAmount(JSONUtils.jsonToAmount(spentTxOutput.getValue()));
				txInput.setTxId(input.getTxid());
				txInput.setVOutIndex(input.getVout());
				txInput.setCoinbase(input.getCoinbase());

				// No need to sort data here.
				tx.getTxInputs().add(txInput);
			}
		});
		// At this point transaction must be correct if not error, we validate it.
		if ((null != tx.getTxId()) && (!withErrorTxIdSet.contains(tx.getTxId()))) {
			validateTx(tx);
		}
	}

	// This can happen because there is a big gap in timebetween getrawmempool and
	// each getrawTransaction when mempool is "full", we track it.
	private void logWarnOnTransactionWithError(GetVerboseRawTransactionResult rawTx, String incompleteTxId) {
		String error;
		if (rawTx == null) {
			error = "GetVerboseRawTransactionResult is null";
		} else {
			if (rawTx.getError() == null) {
				error = "GetVerboseRawTransactionResult error is null";
			} else {
				error = rawTx.getError().toString();
			}
		}
		logger.warn("Error retrieving txId: {} error: {}", incompleteTxId, error);
	}
}
