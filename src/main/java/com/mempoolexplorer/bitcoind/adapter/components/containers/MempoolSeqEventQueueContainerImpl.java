package com.mempoolexplorer.bitcoind.adapter.components.containers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mempoolexplorer.bitcoind.adapter.threads.MempoolSeqEvent;

import org.springframework.stereotype.Component;

@Component
public class MempoolSeqEventQueueContainerImpl implements MempoolSeqEventQueueContainer {

    BlockingQueue<MempoolSeqEvent> blockingQueue = new LinkedBlockingQueue<>();

    @Override
    public BlockingQueue<MempoolSeqEvent> getBlockingQueue() {
        return this.blockingQueue;
    }
}
