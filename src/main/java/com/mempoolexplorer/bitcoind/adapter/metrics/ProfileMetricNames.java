package com.mempoolexplorer.bitcoind.adapter.metrics;

/**
 * 
 * @author tomillo
 *
 *         A collection of metric names just for having them centralized in a
 *         file.
 */
public final class ProfileMetricNames {

	// Can't be constructed.
	private ProfileMetricNames() {
	}

	public static final String MEMPOOL_INITIAL_CREATION_TIME = "mempool.initialcreation.time";
	public static final String MEMPOOL_REFRESH_TIME = "mempool.refresh.time";
	public static final String MEMPOOL_TRANSACTION_COUNT = "mempool.transaction.count";
}
