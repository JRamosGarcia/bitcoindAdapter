package com.mempoolexplorer.bitcoind.adapter.jobs;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolChangesFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.MemPoolException;
import com.mempoolexplorer.bitcoind.adapter.components.mempoolcontainers.changes.TxPoolChangesContainer;
import com.mempoolexplorer.bitcoind.adapter.components.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.components.txpoolcontainers.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.services.TxPoolService;

@DisallowConcurrentExecution
public class MemPoolRefresherJob implements Job {

	Logger logger = LoggerFactory.getLogger(MemPoolRefresherJob.class);

	// Inserted through Quartz Scheduler. Not Spring Container.
	private TxPoolContainer memPoolContainer;

	private TxPoolChangesContainer txPoolChangesContainer;

	private TxPoolChangesFactory txPoolChangesFactory;

	private Boolean saveDBOnRefresh;

	private TxPoolService memPoolService;

	private TxSource txSource;

	private static Boolean firstMemPoolRefresh = Boolean.TRUE;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			TxPoolDiff txPoolDiff = memPoolContainer.refreshTxPool();
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
			// Jobs can only throw JobExecutionException. it will be logged with level=info.
		} catch (MemPoolException e) {
			throw new JobExecutionException(e, false);
		} catch (Throwable e) {
			throw new JobExecutionException(e, false);
		}
	}

	public TxPoolContainer getMemPoolContainer() {
		return memPoolContainer;
	}

	public void setMemPoolContainer(TxPoolContainer memPoolContainer) {
		this.memPoolContainer = memPoolContainer;
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
}
