package com.mempoolexplorer.bitcoind.adapter.components.txpoolcontainers;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.components.factories.InMemTxPoolFillerImpl;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.MemPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.InMemoryTxPoolImp;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.metrics.ProfileMetricNames;
import com.mempoolexplorer.bitcoind.adapter.metrics.annotations.ProfileTime;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * This class is simple a holder of MemPool in Spring component plus a wrapper
 * of memPoolFactory methods.
 * 
 * @author tomillo
 *
 */
@Component
public class TxPoolContainerImpl implements TxPoolContainer {

	@Autowired
	private InMemTxPoolFillerImpl inMemTxPoolFiller;

	
	@Autowired
	private MeterRegistry registry;


	private TxPool txPool = new InMemoryTxPoolImp(new ConcurrentHashMap<>());

	@PostConstruct
	public void registerMetrics() {
		registry.gauge(ProfileMetricNames.MEMPOOL_TRANSACTION_COUNT, txPool, TxPool::getSize);
	}

	@Override
	@ProfileTime(metricName = ProfileMetricNames.MEMPOOL_INITIAL_CREATION_TIME)
	public TxPool createTxPool() throws MemPoolException {

		txPool = inMemTxPoolFiller.createMemPool();

		return txPool;
	}

	@Override
	@ProfileTime(metricName = ProfileMetricNames.MEMPOOL_REFRESH_TIME)
	public TxPoolDiff refreshTxPool() throws MemPoolException {
		TxPoolDiff memPoolDiff = inMemTxPoolFiller.refreshMemPool(txPool);
		txPool.apply(memPoolDiff);
		return memPoolDiff;
	}

	@Override
	public TxPool getTxPool() {
		return txPool;
	}

}
