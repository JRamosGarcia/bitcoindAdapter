package com.mempoolexplorer.bitcoind.adapter.threads;

import org.springframework.stereotype.Component;

/**
 * TODO: Insert class comment here
 */
@Component
public class ZMQSequenceEventReceiver extends ZMQSequenceEventProcessor {
    @Override
    protected void doYourThing() throws InterruptedException {
        int mempoolSequence = 1;
        int zmqSequence = 0;
        while (!endThread) {
            MempoolSeqEvent event = new MempoolSeqEvent("hash", MempoolEventEnum.TXADD, mempoolSequence++,
                    zmqSequence++);
            blockingQueue.add(event);
            Thread.sleep(100000);
        }
    }
}
