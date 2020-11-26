package com.mempoolexplorer.bitcoind.adapter.threads;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolFiller;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.events.MempoolEvent;
import com.mempoolexplorer.bitcoind.adapter.events.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.utils.PercentLog;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
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
    private BlockTemplateContainer blockTemplateContainer;

    @Autowired
    private TxPoolFiller txPoolFiller;
    @Autowired
    private TxSource txSource;

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
        } else {
            log.info("Bitcoind is already working, asking for full mempool and mempoolSequence...");
            TxPool txPool = txPoolFiller.createMemPool();
            txPoolContainer.setTxPool(txPool);
            sendAllMemPoolTxs();// This is an expensive operation, use with care.
            log.info("Full mempool has been queried.");
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
        Optional<TxPoolChanges> txPoolChanges = txPoolFiller.obtainOnTxMemPoolChanges(event);
        int eventMPS = event.getMempoolSequence().orElseThrow(onNoSeqNumberExceptionSupplier(event));
        TxPool txPool = txPoolContainer.getTxPool();
        // Update our mempool
        txPoolChanges.ifPresentOrElse(txPC -> txPool.apply(txPC, eventMPS), () -> txPool.apply(eventMPS));
        // Send to kafka.
        txPoolChanges.ifPresent(txPC -> txSource
                .publishMemPoolEvent(MempoolEvent.createFrom(txPC, blockTemplateContainer.getChanges())));
        // Log update if any
        txPoolChanges.ifPresent(txPC -> {
            if (log.isDebugEnabled() && !txPC.getTxAncestryChangesMap().isEmpty())
                log.debug(txPC.getTxAncestryChangesMap().toString());
        });
    }

    private void onBlock(MempoolSeqEvent event) {
        // No mempoolSequence for block events
        Optional<Pair<TxPoolChanges, Block>> opPair = txPoolFiller.obtainOnBlockMemPoolChanges(event);
        TxPool txPool = txPoolContainer.getTxPool();
        if (opPair.isEmpty()) {
            return;// Error logging on txPoolFiller.
        }

        TxPoolChanges txPoolChanges = opPair.get().getLeft();
        Block block = opPair.get().getRight();

        // Update our mempool
        txPool.apply(txPoolChanges);
        // Log update if any
        if (log.isDebugEnabled() && !txPoolChanges.getTxAncestryChangesMap().isEmpty())
            log.debug(txPoolChanges.getTxAncestryChangesMap().toString());

        // First we send block to kafka.
        txSource.publishMemPoolEvent(MempoolEvent.createFrom(block));
        // Then we send txPoolChanges of that block
        txSource.publishMemPoolEvent(MempoolEvent.createFrom(txPoolChanges, blockTemplateContainer.getChanges()));
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
        // Reset downstream counter to 0 to provoke cascade resets.
        txPoolFiller.resetChangeCounter();
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

    /**
     * Sends all memPool transactions 10 by 10
     */
    private void sendAllMemPoolTxs() {
        Map<String, Transaction> fullTxPool = txPoolContainer.getTxPool().getFullTxPool();
        TxPoolChanges txpc = new TxPoolChanges();
        // All change counter are set to 0, signaling to clients that they must forget
        // previous mempool and refresh
        txpc.setChangeCounter(0);
        txpc.setChangeTime(Instant.now());

        PercentLog pl = new PercentLog(fullTxPool.size());
        int counter = 0;
        Iterator<Entry<String, Transaction>> it = fullTxPool.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Transaction> entry = it.next();

            if (txpc.getNewTxs().size() == 10) {
                txSource.publishMemPoolEvent(MempoolEvent.createFrom(txpc, Optional.empty()));
                txpc.setNewTxs(new ArrayList<>(10));
                pl.update(counter, percent -> log.info("Sending full txMemPool: {}", percent));
            }
            txpc.getNewTxs().add(entry.getValue());
            counter++;
        }

        if (!txpc.getNewTxs().isEmpty()) {
            txSource.publishMemPoolEvent(MempoolEvent.createFrom(txpc, Optional.empty()));
            pl.update(counter, percent -> log.info("Sending full txMemPool: {}", percent));
        }
    }
}
