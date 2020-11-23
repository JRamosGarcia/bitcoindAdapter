package com.mempoolexplorer.bitcoind.adapter.jobs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResultData;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionInput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionOutput;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetVerboseRawTransactionResultData;
import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.BlockFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolFiller;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.CoinBaseTx;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.NotInMemPoolTx;
import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;
import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplateChanges;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.events.MempoolEvent;
import com.mempoolexplorer.bitcoind.adapter.events.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindAdapterProperties;
import com.mempoolexplorer.bitcoind.adapter.utils.JSONUtils;
import com.mempoolexplorer.bitcoind.adapter.utils.PercentLog;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DisallowConcurrentExecution
public class MemPoolRefresherJob implements Job {

	Logger logger = LoggerFactory.getLogger(MemPoolRefresherJob.class);

	// Inserted through Quartz Scheduler. Not Spring Container.
	private TxPoolContainer memPoolContainer;
	private BlockFactory blockFactory;
	private TxSource txSource;
	private TxPoolFiller txPoolFiller;
	private BitcoindClient bitcoindClient;
	private BlockTemplateContainer blockTemplateContainer;
	private BitcoindAdapterProperties bitcoindAdapterProperties;
	private AlarmLogger alarmLogger;
	private MemPoolRefresherJobState state;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			// New blocks are sent before changes in mempool, but if this is the first shot,
			// we ignore new block if any
			if (!state.isFirstMemPoolRefresh()) {
				sendIfNewBlock();
			}
			refreshMempoolAndSendChanges();

		} catch (TxPoolException | RuntimeException e) {
			alarmLogger.addAlarm("Exception: " + e.getMessage());
			logger.error("Exception: ", e);
		}

	}

	private void sendIfNewBlock() {
		Integer blockNumNow = bitcoindClient.getBlockCount();
		if (state.getBlockNum() == -1) {
			state.setBlockNum(blockNumNow);
		}
		while (state.getBlockNum() < blockNumNow) {
			state.setBlockNum(state.getBlockNum() + 1);
			GetBlockResult getBlockResult = bitcoindClient.getBlock(state.getBlockNum());
			logger.info("New block with height: {} and hash: {}. Sending msg to msgQueue", state.getBlockNum(),
					getBlockResult.getGetBlockResultData().getHash());

			Block block = blockFactory.from(getBlockResult.getGetBlockResultData());
			addNotInMemPoolTxsOf(block);
			txSource.publishMemPoolEvent(MempoolEvent.createFrom(block));
		}
	}

	// After finding a new block, it (normally) could be the case that we don't have
	// all of the transactions. (i.e. coinbase or transactions not relayed to us).
	// We need some of that transactions data for statistics
	private void addNotInMemPoolTxsOf(Block block) {

		// First we obtain the list of transactions in the block which are not in the
		// memPool
		List<String> notInMemPoolTxIds = block.getTxIds().stream()
				.filter(txId -> null == memPoolContainer.getTxPool().getTx(txId)).collect(Collectors.toList());

		// Then we construct NotInMemPoolTx or coinbase data and add it to the block
		for (String txId : notInMemPoolTxIds) {
			GetVerboseRawTransactionResultData txData = bitcoindClient.getVerboseRawTransaction(txId)
					.getGetRawTransactionResultData();
			String coinbase = txData.getVin().get(0).getCoinbase();
			if (null == coinbase || coinbase.isEmpty()) {// Not coinbase tx
				Long inputsAmount = getInputsAmount(txData.getVin());
				Long outputsAmount = getOutputsAmount(txData.getVout());
				Long fee = inputsAmount - outputsAmount;
				Integer weight = txData.getWeight();
				// TODO: Sadly, It's a nigthmare get a fee with ancestors. yet...
				block.getNotInMemPoolTransactions().put(txId, new NotInMemPoolTx(txId, fee, weight));
			} else {// coinbase tx
				CoinBaseTx coinBaseTx = new CoinBaseTx();
				coinBaseTx.setTxId(txId);
				coinBaseTx.setvInField(coinbase);
				coinBaseTx.setWeight(txData.getWeight());
				block.setCoinBaseTx(coinBaseTx);
			}
		}
	}

	// Gets the sum of values in satoshis of all txInputs, we have to ask for the
	// txin.txId output and index
	private Long getInputsAmount(List<GetVerboseRawTransactionInput> txin) {
		return txin.stream().mapToLong(txIn -> {
			String txId = txIn.getTxid();
			Integer index = txIn.getVout();
			GetVerboseRawTransactionResultData inputTxData = bitcoindClient.getVerboseRawTransaction(txId)
					.getGetRawTransactionResultData();
			return JSONUtils.jsonToAmount(inputTxData.getVout().get(index).getValue());
		}).sum();
	}

	private Long getOutputsAmount(List<GetVerboseRawTransactionOutput> vout) {
		return vout.stream().mapToLong(txOut -> JSONUtils.jsonToAmount(txOut.getValue())).sum();
	}

	private void refreshMempoolAndSendChanges() throws TxPoolException {
		Integer blockNumBefore = bitcoindClient.getBlockCount();
		// This two next calls are independent one from the other. So, by race
		// conditions, INEVITABLY (but rare), It's possible to have txs that does not
		// appear in the other set. (i.e. a tx is added and then removed between calls)
		GetBlockTemplateResultData getBlockTemplateResultData = bitcoindClient.getBlockTemplateResult()
				.getGetBlockTemplateResultData();
		TxPoolChanges txPoolChanges = txPoolFiller.obtainMemPoolChanges(memPoolContainer.getTxPool());
		Integer blockNumAfter = bitcoindClient.getBlockCount();

		if (blockNumBefore.compareTo(blockNumAfter) != 0) {
			logger.info("There is a new block in-between refresh, not sending diffs");
			return; // If there is a new block in-between we do not refresh mempool or send diffs
		}
		memPoolContainer.getTxPool().apply(txPoolChanges);

		// Export changes to REST Service and MsgQueue only if there are changes

		if (state.isFirstMemPoolRefresh()) {
			state.setFirstMemPoolRefresh(false);
			if (bitcoindAdapterProperties.isSendAllTxOnStart()) {
				logger.info(
						"Mempool Refreshed by the first time. All messages will be sent to msgQueue. This can take a long time...");
				sendAllMemPoolTxs();// This is an expensive operation, use with care.
			} else {
				// FirstTime this is executed the message equals mempoolSize if no fresh txs are
				// in db. So better let others converge to your mempool slowly or by other
				// means.
				logger.info("Mempool Refreshed by the first time. No message will be sent to msgQueue");
			}
		} else {
			if (txPoolChanges.hasChanges()) {
				BlockTemplateChanges blockTemplateChanges = calculateBlockTemplateChanges(getBlockTemplateResultData);
				logger.info("Mempool Refreshed, sending msg txPoolChanges({}) to msgQueue",
						txPoolChanges.getChangeCounter());
				txSource.publishMemPoolEvent(MempoolEvent.createFrom(txPoolChanges, Optional.of(blockTemplateChanges)));
			}
		}
		logger.info("{} transactions in txMemPool.", memPoolContainer.getTxPool().getSize());
	}

	private BlockTemplateChanges calculateBlockTemplateChanges(GetBlockTemplateResultData getBlockTemplateResultData) {
		BlockTemplate newBT = new BlockTemplate(getBlockTemplateResultData);
		BlockTemplate oldBT = blockTemplateContainer.getBlockTemplate();
		BlockTemplateChanges blockTemplateChanges = new BlockTemplateChanges(newBT, oldBT);
		blockTemplateContainer.setBlockTemplate(newBT);
		logger.info("new BlockTemplate(size: {} new: {} remove: {})", newBT.getBlockTemplateTxMap().size(),
				blockTemplateChanges.getAddBTTxsList().size(), blockTemplateChanges.getRemoveBTTxIdsList().size());
		return blockTemplateChanges;
	}

	/**
	 * Sends all memPool transactions 10 by 10
	 */
	private void sendAllMemPoolTxs() {
		Map<String, Transaction> fullTxPool = memPoolContainer.getTxPool().getFullTxPool();
		TxPoolChanges txpc = new TxPoolChanges();
		// All change counter are set to 0, signaling to clients that they must forget
		// previous mempool and refresh
		txpc.setChangeCounter(0);
		txpc.setChangeTime(Instant.now());

		PercentLog pl = new PercentLog(fullTxPool.size());
		int counter = 0;
		Iterator<Entry<String, Transaction>> it = fullTxPool.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Transaction> entry = it.next();

			if (txpc.getNewTxs().size() == 10) {
				txSource.publishMemPoolEvent(MempoolEvent.createFrom(txpc, Optional.empty()));
				txpc.setNewTxs(new ArrayList<>(10));
				pl.update(counter, (percent) -> logger.info("Sending full txMemPool: {}", percent));
			}
			txpc.getNewTxs().add(entry.getValue());
			counter++;
		}

		if (!txpc.getNewTxs().isEmpty()) {
			txSource.publishMemPoolEvent(MempoolEvent.createFrom(txpc, Optional.empty()));
			pl.update(counter, percent -> logger.info("Sending full txMemPool: {}", percent));
		}
	}
}
