package com.mempoolexplorer.bitcoind.adapter.events;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface CustomChannels {
	@Qualifier("txMemPoolEventsChannel")
	@Output("txMemPoolEvents")
    MessageChannel txMemPoolEventsChannel();
}