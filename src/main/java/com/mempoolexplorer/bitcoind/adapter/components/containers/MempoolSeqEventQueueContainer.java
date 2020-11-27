package com.mempoolexplorer.bitcoind.adapter.components.containers;

import java.util.concurrent.BlockingQueue;

import com.mempoolexplorer.bitcoind.adapter.threads.MempoolSeqEvent;

public interface MempoolSeqEventQueueContainer {

    BlockingQueue<MempoolSeqEvent> getBlockingQueue();

}
