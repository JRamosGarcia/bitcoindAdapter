package com.mempoolexplorer.bitcoind.adapter.components.containers.txpool;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolImp;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.metrics.ProfileMetricNames;

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
	private MeterRegistry registry;


	private TxPool txPool = new TxPoolImp(new ConcurrentHashMap<>());

	@PostConstruct
	public void registerMetrics() {
		registry.gauge(ProfileMetricNames.MEMPOOL_TRANSACTION_COUNT, txPool, TxPool::getSize);
	}

	@Override
	public void setTxPool(TxPool txPool){
		this.txPool=txPool;
	}

	@Override
	public TxPool getTxPool() {
		return txPool;
	}

}
