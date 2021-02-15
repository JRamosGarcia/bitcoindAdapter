package com.mempoolexplorer.bitcoind.adapter.threads;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;

import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolFiller;
import com.mempoolexplorer.bitcoind.adapter.components.health.MempoolSyncedHealthIndicator;
import com.mempoolexplorer.bitcoind.adapter.entities.Transaction;
import com.mempoolexplorer.bitcoind.adapter.entities.blockchain.changes.Block;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.changes.TxPoolChanges;
import com.mempoolexplorer.bitcoind.adapter.events.MempoolEvent;
import com.mempoolexplorer.bitcoind.adapter.events.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.jobs.BlockTemplateRefresherJob;
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
    private BlockTemplateRefresherJob blockTemplateRefresherJob;
    @Autowired
    private TxPoolFiller txPoolFiller;
    @Autowired
    private TxSource txSource;
    @Autowired
    private MempoolSyncedHealthIndicator mempoolSyncedHealthIndicator;


    private boolean isStarting = true;

    // Incoming seqNumber
    private int lastZMQSequence = -1;

    // Outcoming seqNumber
    private int downStreamSeqNumber = -1;

    @Override
    protected void doYourThing() throws InterruptedException {
        try {
            while (!endThread) {
                MempoolSeqEvent event = null;
                event = blockingQueueContainer.getBlockingQueue().take();
                log.debug("This is the event: {}", event);
                onEvent(event);
            }
        } catch (Exception e) {
            // We cannot recover from this. fastFail
            log.error("", e);
            alarmLogger.addAlarm("Fatal error" + ExceptionUtils.getStackTrace(e));
        }
    }

    private void onEvent(MempoolSeqEvent event) throws InterruptedException {
        // Checks if BlockTemplateRefresherJob must start. (only when all tx has been
        // loaded)
        checkForBTRefresherStart();
        if (isStarting) {
            // ResetContainers or Queries full mempool with mempoolSequence number.
            onEventonStarting(event);
            isStarting = false;
        }
        treatEvent(event);
    }

    private void checkForBTRefresherStart() throws InterruptedException {
        if (isStarting) {
            // When starting better stop for 1 second to let ZMQEventQueue to fill. And then
            // ask for its size==0 to start Job.
            Thread.sleep(1000);
            return;
        }
        // if no pending Txs or blocks, then we can start job.
        if (blockingQueueContainer.getBlockingQueue().isEmpty() && (!blockTemplateRefresherJob.isStarted())) {
            blockTemplateRefresherJob.setStarted(true);
            log.info("BlockTemplateRefresherJob started");
            // Execute ASAP. does not matter if scheduller also invokes it. It's thread
            // safe.
            blockTemplateRefresherJob.execute();
        }
    }

    private void onEventonStarting(MempoolSeqEvent event) throws InterruptedException {
        if (event.getZmqSequence() == 0) {
            log.info("Bitcoind is starting while we are already up");
            // We don't need resetContainers in case of bitcoind crash, onErrorInZMQSequence
            // has done it for us.
        } else {
            log.info("Bitcoind is already working, asking for full mempool and mempoolSequence...");

            TxPool txPool = null;
            while (txPool == null) {
                try {
                    txPool = txPoolFiller.createMemPool();
                } catch (Exception e) {
                    log.warn("Bitcoind is not ready yet waiting 5 seconds...");
                    alarmLogger.addAlarm("Bitcoind is not ready yet waiting 5 seconds...");
                    Thread.sleep(5000);
                }
            }
            txPoolContainer.setTxPool(txPool);
            log.info("Full mempool has been queried.");
            sendAllMemPoolTxs();// This is an expensive operation, use with care.
        }
        // Fake a lastZMQSequence because we are starting
        lastZMQSequence = event.getZmqSequence() - 1;
        mempoolSyncedHealthIndicator.setMempoolSynced(true);
    }

    private void treatEvent(MempoolSeqEvent event) throws InterruptedException {

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

    private void onTx(MempoolSeqEvent event) {
        // Note: events could be discarded if currentMPS >= eventMPS (after loading a
        // full mempool). but we have found it causes tx loss. why?. don't know but we
        // will not discard any event.
        Optional<TxPoolChanges> txPoolChanges = txPoolFiller.obtainOnTxMemPoolChanges(event);
        int eventMPS = event.getMempoolSequence().orElseThrow(onNoSeqNumberExceptionSupplier(event));
        TxPool txPool = txPoolContainer.getTxPool();
        // Update our mempool
        txPoolChanges.ifPresentOrElse(txPC -> txPool.apply(txPC, eventMPS), () -> txPool.apply(eventMPS));
        // Send to kafka.
        txPoolChanges.ifPresent(txPC -> txSource
                .publishMemPoolEvent(MempoolEvent.createFrom(txPC, ++downStreamSeqNumber, txPool.getSize())));
        // Log update if any
        txPoolChanges.ifPresent(txPC -> {
            if (log.isDebugEnabled() && !txPC.getTxAncestryChangesMap().isEmpty())
                log.debug(txPC.getTxAncestryChangesMap().toString());
        });
    }

    private void onBlock(MempoolSeqEvent event) {
        // Since a new block has arrived, we want to force as soon as possible a
        // blockTemplate refresh
        forceRefreshBlockTemplate();

        // No mempoolSequence for block events
        Optional<Pair<TxPoolChanges, Block>> opPair = txPoolFiller.obtainOnBlockMemPoolChanges(event);
        TxPool txPool = txPoolContainer.getTxPool();
        if (opPair.isEmpty()) {
            return;// Error. Logging on txPoolFiller.
        }

        TxPoolChanges txPoolChanges = opPair.get().getLeft();
        Block block = opPair.get().getRight();

        // Update our mempool
        txPool.apply(txPoolChanges);
        // Log update if any
        if (log.isDebugEnabled() && !txPoolChanges.getTxAncestryChangesMap().isEmpty())
            log.debug(txPoolChanges.getTxAncestryChangesMap().toString());

        // We send block to kafka with the related changes in mempool, and also
        // blockTemplate changes, note that blockTemplate is Optional because we can
        // have not a blocktemplate due to: two consecutive blocks or block
        // disconnection.
        txSource.publishMemPoolEvent(MempoolEvent.createFrom(block, txPoolChanges,
                blockTemplateContainer.pull(block.getHeight()), ++downStreamSeqNumber, txPool.getSize()));
    }

    private void forceRefreshBlockTemplate() {
        blockTemplateRefresherJob.execute();
    }

    private void fullReset() {
        // We stops blockTemplateRefresherJob
        blockTemplateRefresherJob.setStarted(false);
        log.info("BlockTemplateRefresherJob stopped");
        resetContainers();
        // Reset downstream counter to provoke cascade resets.
        downStreamSeqNumber = -1;
        isStarting = true;
        lastZMQSequence = -1;
        mempoolSyncedHealthIndicator.setMempoolSynced(false);
    }

    private void onErrorInZMQSequence(MempoolSeqEvent event) throws InterruptedException {
        // Somehow we have lost mempool events. We have to re-start again.
        log.error("We have lost a ZMQMessage, ZMQSequence not expected: {}, "
                + "Reset and waiting for new full mempool and mempoolSequence...", event.getZmqSequence());
        fullReset();
        // Event is reintroduced and it's not lost never. This is a must when bitcoind
        // re-starts and send a ZMQSequenceEvent = 0
        onEvent(event);
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
        blockTemplateContainer.drop();
    }

    /**
     * Sends all memPool transactions 10 by 10
     */
    private void sendAllMemPoolTxs() {
        log.info("Sending full mempool downstream...");
        Map<String, Transaction> fullTxPool = txPoolContainer.getTxPool().getFullTxPool();
        TxPoolChanges txpc = new TxPoolChanges();
        // All change counter are set to 0, signaling to clients that they must forget
        // previous mempool and refresh

        PercentLog pl = new PercentLog(fullTxPool.size());
        int counter = 0;
        Iterator<Entry<String, Transaction>> it = fullTxPool.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Transaction> entry = it.next();

            if (txpc.getNewTxs().size() == 10) {
                txSource.publishMemPoolEvent(MempoolEvent.createFrom(txpc, ++downStreamSeqNumber, -1));
                txpc.setNewTxs(new ArrayList<>(10));
                pl.update(counter, percent -> log.info("Sending full txMemPool: {}", percent));
            }
            txpc.getNewTxs().add(entry.getValue());
            counter++;
        }

        if (!txpc.getNewTxs().isEmpty()) {
            txSource.publishMemPoolEvent(MempoolEvent.createFrom(txpc, ++downStreamSeqNumber, -1));
            pl.update(counter, percent -> log.info("Sending full txMemPool: {}", percent));
        }
        log.info("Full mempool has been sent downstream...");
    }
}
