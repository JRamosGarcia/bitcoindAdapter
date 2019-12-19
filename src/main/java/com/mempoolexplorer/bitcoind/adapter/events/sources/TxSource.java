package com.mempoolexplorer.bitcoind.adapter.events.sources;

import com.mempoolexplorer.bitcoind.adapter.events.MempoolEvent;

public interface TxSource {

	void publishMemPoolEvent(MempoolEvent memPoolEvent);
	
}