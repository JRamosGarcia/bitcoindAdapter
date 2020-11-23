package com.mempoolexplorer.bitcoind.adapter.threads;

import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Deques ZMQ sequence events and treats them acordingly.
 * 
 * When starting, checks if zmqSequence==0, in that case bitcoind is starting
 * and all it's mempool will be received as MempoolSeqEvents. If not, then a
 * bitcoindClient.getRawMempoolNonVerbose will be called and a (int)
 * mempoolSequence obtained. Then, we discard mempoolSequenceEvents until it's
 * mempoolSequence matches the later. See
 * https://github.com/bitcoin/bitcoin/blob/master/doc/zmq.md#usage
 * 
 * Also, checks if zmqSequence is complete (no gaps). If there is a gap a
 * mempool reset needs to be done, because we have lost txs.
 * 
 * MempoolSequenceEvents for block connection and disconnection causes a
 * bitcoindClient.getBlock call and tx removal or addition respectively.
 * 
 * Be aware that mempoolSequence starts in 1 and zmqSequence starts in 0
 * mempoolSequence=Optional[1], zmqSequence=0
 * 
 */
@Component
@Slf4j
public class ZMQSequenceEventConsumer extends ZMQSequenceEventProcessor {

    @Autowired
    private TxPoolContainer txPoolContainer;

    @Autowired
    private BitcoindClient bitcoindClient;

    private boolean isStarting = true;
    private int lastZMQSequence = -1;

    @Override
    protected void doYourThing() throws InterruptedException {
        while (!endThread) {
            MempoolSeqEvent event = null;
            event = blockingQueue.take();
            log.info("This is the event: {}", event);
            treatEvent(event);
        }
    }

    private void treatEvent(MempoolSeqEvent event) {
        // Bitcoind is starting while we are already up
        if (isStarting && event.getZmqSequence() == 0) {
            // Maybe bitcoind has gone down and we are up. Reset mempool and containers.
            resetContainers();
            isStarting = false;
        }
    }

    private void resetContainers() {
        txPoolContainer.getTxPool().drop();

    }

}
