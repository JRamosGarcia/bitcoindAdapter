package com.mempoolexplorer.bitcoind.adapter.components.sources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

@Component
public class TxSourceImpl implements TxSource {

	@Autowired
	private Source source;

	@Override
	public void publishTxChanges(TxPoolChanges txPoolChanges) {
		source.output().send(MessageBuilder.withPayload(txPoolChanges).build());
	}
	
	
}
