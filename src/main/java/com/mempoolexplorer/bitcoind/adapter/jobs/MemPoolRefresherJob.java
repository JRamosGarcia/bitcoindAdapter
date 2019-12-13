package com.mempoolexplorer.bitcoind.adapter.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResult;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.blockchain.changes.LastBlocksContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.changes.TxPoolChangesContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.BlockFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolChangesFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolFiller;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.MemPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.events.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.services.TxPoolService;

@DisallowConcurrentExecution
public class MemPoolRefresherJob implements Job {

	Logger logger = LoggerFactory.getLogger(MemPoolRefresherJob.class);

	// Inserted through Quartz Scheduler. Not Spring Container.
	private TxPoolContainer memPoolContainer;

	private LastBlocksContainer lastBlocksContainer;

	private TxPoolChangesContainer txPoolChangesContainer;

	private BlockFactory blockFactory;

	private TxPoolChangesFactory txPoolChangesFactory;

	private Boolean saveDBOnRefresh;

	private TxPoolService memPoolService;

	private TxSource txSource;

	private TxPoolFiller txPoolFiller;

	private BitcoindClient bitcoindClient;

	private static Boolean firstMemPoolRefresh = Boolean.TRUE;

	private static Integer blockNum = null;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {

			// New blocks are sent before changes in mempool
			sendIfNewBlock();

			// TODO: Hacer que no se mande si hay bloque nuevo.
			refreshMempoolAndSendChanges();

			// Jobs can only throw JobExecutionException. it will be logged with level=info.
		} catch (MemPoolException e) {
			throw new JobExecutionException(e, false);
		} catch (Throwable e) {
			throw new JobExecutionException(e, false);
		}
	}

	private void sendIfNewBlock() {
		Integer blockNumNow = bitcoindClient.getBlockCount();
		if (blockNum == null) {
			blockNum = blockNumNow;
		}
		while (blockNum < blockNumNow) {
			GetBlockResult getBlockResult = bitcoindClient.getBlock(blockNumNow);
			logger.info("New block with height: " + blockNumNow + " and hash:"
					+ getBlockResult.getGetBlockResultData().getHash() + ". Sending msg to msgQueue");
			
			Block block = blockFactory.from(getBlockResult.getGetBlockResultData());
			lastBlocksContainer.add(block);
						
			txSource.publishNewBlock(getBlockResult.getGetBlockResultData());
			blockNum++;
		}
	}

	private void refreshMempoolAndSendChanges() throws MemPoolException {
		Integer blockNumbefore = bitcoindClient.getBlockCount();
		TxPoolDiff txPoolDiff = txPoolFiller.obtainMemPoolDiffs(memPoolContainer.getTxPool());
		Integer blockNumAfter = bitcoindClient.getBlockCount();

		if (blockNumbefore.compareTo(blockNumAfter)!=0) {
			logger.info("There is a new block in-between refresh, not sending diffs");
			return; // If there is a new block in-between we do not refresh mempool or send diffs
		}

		memPoolContainer.getTxPool().apply(txPoolDiff);

		if (saveDBOnRefresh) {
			memPoolService.apply(txPoolDiff);
		}

		// Export changes to REST Service and MsgQueue only if there are changes
		if (txPoolDiff.hasChanges()) {
			TxPoolChanges txPoolChanges = txPoolChangesFactory.from(txPoolDiff);
			txPoolChangesContainer.add(txPoolChanges);

			// TODO: Make this better
			// FirstTime this is executed the message equals mempoolSize if no fresh txs are
			// in db. So better let others converge to your mempool slowly or by other
			// means.
			if (firstMemPoolRefresh) {
				firstMemPoolRefresh = Boolean.FALSE;
				logger.info("Mempool Refreshed by the first time. No message will be sent to msgQueue");
			} else {
				logger.info("Mempool Refreshed, sending msg to msgQueue");
				txSource.publishTxChanges(txPoolChanges);
			}
		}
	}

	public TxPoolContainer getMemPoolContainer() {
		return memPoolContainer;
	}

	public void setMemPoolContainer(TxPoolContainer memPoolContainer) {
		this.memPoolContainer = memPoolContainer;
	}

	public LastBlocksContainer getLastBlocksContainer() {
		return lastBlocksContainer;
	}

	public void setLastBlocksContainer(LastBlocksContainer lastBlocksContainer) {
		this.lastBlocksContainer = lastBlocksContainer;
	}

	public TxPoolChangesContainer getMemPoolChangesContainer() {
		return txPoolChangesContainer;
	}

	public void setMemPoolChangesContainer(TxPoolChangesContainer memPoolChangesContainer) {
		this.txPoolChangesContainer = memPoolChangesContainer;
	}

	public TxPoolChangesFactory getMemPoolChangesFactory() {
		return txPoolChangesFactory;
	}

	public void setMemPoolChangesFactory(TxPoolChangesFactory memPoolChangesFactory) {
		this.txPoolChangesFactory = memPoolChangesFactory;
	}

	public Boolean getSaveDBOnRefresh() {
		return saveDBOnRefresh;
	}

	public void setSaveDBOnRefresh(Boolean saveDBOnRefresh) {
		this.saveDBOnRefresh = saveDBOnRefresh;
	}

	public TxPoolService getMemPoolService() {
		return memPoolService;
	}

	public void setMemPoolService(TxPoolService memPoolService) {
		this.memPoolService = memPoolService;
	}

	public TxSource getTxSource() {
		return txSource;
	}

	public void setTxSource(TxSource txSource) {
		this.txSource = txSource;
	}

	public TxPoolChangesContainer getTxPoolChangesContainer() {
		return txPoolChangesContainer;
	}

	public void setTxPoolChangesContainer(TxPoolChangesContainer txPoolChangesContainer) {
		this.txPoolChangesContainer = txPoolChangesContainer;
	}

	public BlockFactory getBlockFactory() {
		return blockFactory;
	}

	public void setBlockFactory(BlockFactory blockFactory) {
		this.blockFactory = blockFactory;
	}

	public TxPoolChangesFactory getTxPoolChangesFactory() {
		return txPoolChangesFactory;
	}

	public void setTxPoolChangesFactory(TxPoolChangesFactory txPoolChangesFactory) {
		this.txPoolChangesFactory = txPoolChangesFactory;
	}

	public TxPoolFiller getTxPoolFiller() {
		return txPoolFiller;
	}

	public void setTxPoolFiller(TxPoolFiller txPoolFiller) {
		this.txPoolFiller = txPoolFiller;
	}

	public BitcoindClient getBitcoindClient() {
		return bitcoindClient;
	}

	public void setBitcoindClient(BitcoindClient bitcoindClient) {
		this.bitcoindClient = bitcoindClient;
	}

}
