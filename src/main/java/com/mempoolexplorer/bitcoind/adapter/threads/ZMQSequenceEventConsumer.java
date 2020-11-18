package com.mempoolexplorer.bitcoind.adapter.threads;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * TODO: insert your comment here
 */
@Component
@Slf4j
public class ZMQSequenceEventConsumer extends ZMQSequenceEventProcessor {
    @Override
    protected void doYourThing() throws InterruptedException {
        while (!endThread) {
            MempoolSeqEvent event = null;
            event = blockingQueue.take();
            log.info("This is the event: {}", event);
        }
    }

}
