package com.mempoolexplorer.bitcoind.adapter.threads;

import java.util.function.Supplier;

import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolFiller;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;

import org.apache.commons.lang.exception.ExceptionUtils;
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
 * Also, checks if zmqSequence and mempoolSequence are complete (no gaps). If
 * there is a gap a mempool reset needs to be done, because we have lost txs.
 * ZMQ msgs are not 100% reliable.
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
    private TxPoolFiller txPoolFiller;

    @Autowired
    private AlarmLogger alarmLogger;

    private boolean isStarting = true;
    private int lastZMQSequence = -1;

    @Override
    protected void doYourThing() throws InterruptedException {
        try {
            while (!endThread) {
                MempoolSeqEvent event = null;
                event = blockingQueue.take();
                log.debug("This is the event: {}", event);
                onEvent(event);
            }
        } catch (TxPoolException e) {
            // We cannot recover from this. fastFail
            log.error("", e);
            alarmLogger.addAlarm("Fatal error" + ExceptionUtils.getStackTrace(e));
        }
    }

    private void onEvent(MempoolSeqEvent event) throws TxPoolException {
        if (isStarting) {
            // ResetContainers or Queries full mempool with mempoolSequence number.
            onEventonStarting(event);
            isStarting = false;
        }
        treatEvent(event);
    }

    private void onEventonStarting(MempoolSeqEvent event) throws TxPoolException {
        if (event.getZmqSequence() == 0) {
            log.info("Bitcoind is starting while we are already up");
            resetContainers();// in case of bitcoind crash
            isStarting = false;
        } else {
            log.info("Bitcoind is already working, asking for full mempool and mempoolSequence...");
            TxPool txPool = txPoolFiller.createMemPool();
            txPoolContainer.setTxPool(txPool);
            log.info("Full mempool has been queried.");
            isStarting = false;
        }
        // Fake a lastZMQSequence because we are starting
        lastZMQSequence = event.getZmqSequence() - 1;
    }

    private void treatEvent(MempoolSeqEvent event) throws TxPoolException {

        if (errorInZMQSequence(event)) {
            onErrorInZMQSequence(event);// Makes a full reset
            return;
        }
        switch (event.getEvent()) {
            case TXADD:
            case TXDEL:
                onTx(event);
                break;
            case BLOCKCON:
            case BLOCKDIS:
                onBlock(event);
                break;
            default:
                throw new IllegalArgumentException("unrecognized event type");
        }
    }

    private void onTx(MempoolSeqEvent event) throws TxPoolException {
        // Events can be discarded if currentMPS >= eventMPS
        if (discardEventAndLogIt(event)) {
            return;
        }
        TxPoolChanges txPoolChanges = txPoolFiller.obtainMemPoolChanges(event);
        int eventMPS = event.getMempoolSequence().orElseThrow(onNoSeqNumberExceptionSupplier(event));
        txPoolContainer.getTxPool().apply(txPoolChanges, eventMPS);
    }

    private void onBlock(MempoolSeqEvent event) {
        // No mempoolSequence for block events
        TxPoolChanges txPoolChanges = txPoolFiller.obtainMemPoolChanges(event);
        txPoolContainer.getTxPool().apply(txPoolChanges);
    }

    private boolean discardEventAndLogIt(MempoolSeqEvent event) {
        int currentMPS = txPoolContainer.getTxPool().getMempoolSequence();
        int eventMPS = event.getMempoolSequence().orElseThrow(onNoSeqNumberExceptionSupplier(event));
        // Events can be discarded if currentMPS >= eventMPS
        if (currentMPS >= eventMPS) {
            log.info("Event discarded, current mempool sequence: {}, event: {}", currentMPS, eventMPS);
            return true;
        }
        return false;
    }

    private void fullReset() throws TxPoolException {
        resetContainers();
        isStarting = true;
        lastZMQSequence = -1;
    }

    private void onErrorInZMQSequence(MempoolSeqEvent event) throws TxPoolException {
        // Somehow we have lost mempool events. We have to re-start again.
        log.error("We have lost a ZMQMessage, ZMQSequence not expected: {}, "
                + "asking for new full mempool and mempoolSequence...", event.getZmqSequence());
        fullReset();
    }

    private boolean errorInZMQSequence(MempoolSeqEvent event) {
        return ((++lastZMQSequence) != event.getZmqSequence());
    }

    private Supplier<IllegalArgumentException> onNoSeqNumberExceptionSupplier(MempoolSeqEvent event) {
        return () -> new IllegalArgumentException(
                "EventType: " + event.getEvent().toString() + " does not have mempoolSequence");
    }

    private void resetContainers() {
        txPoolContainer.getTxPool().drop();
        // BlockTemplateContainer no needs to reset
    }

}
