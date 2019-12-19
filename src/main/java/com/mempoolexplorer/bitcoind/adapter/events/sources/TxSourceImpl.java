package com.mempoolexplorer.bitcoind.adapter.events.sources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockResultData;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.events.CustomChannels;
import com.mempoolexplorer.bitcoind.adapter.events.MempoolEvent;

@EnableBinding(CustomChannels.class)
public class TxSourceImpl implements TxSource {

	@Autowired
	@Qualifier("txMemPoolEventsChannel")
	private MessageChannel txMemPoolEventsChannel;

	@Override
	public void publishMemPoolEvent(MempoolEvent memPoolEvent) {
		txMemPoolEventsChannel.send(MessageBuilder.withPayload(memPoolEvent).build());
		
	}

}
