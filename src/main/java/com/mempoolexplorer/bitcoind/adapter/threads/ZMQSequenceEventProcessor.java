package com.mempoolexplorer.bitcoind.adapter.threads;

import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.containers.MempoolSeqEventQueueContainer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

/**
 * Interface that defines a ZMQ sequence event processor. As a producer or as a
 * consumer a ZMQSequenceEventProcessor shares a blockingQueue.
 */
@Slf4j
public abstract class ZMQSequenceEventProcessor implements Runnable {

    private boolean started = false;
    private boolean finished = false;
    private Thread thread = null;
    protected boolean endThread;

    @Autowired
    protected MempoolSeqEventQueueContainer blockingQueueContainer;

    @Autowired
    protected AlarmLogger alarmLogger;

    protected abstract void doYourThing() throws InterruptedException;

    public void start() {
        if (finished)
            throw new IllegalStateException("This class only accepts only one start");
        thread = new Thread(this);
        thread.start();
        started = true;
    }

    public void shutdown() {
        if (!started)
            throw new IllegalStateException("This class is not started yet!");
        endThread = true;
        thread.interrupt();// In case thread is waiting for something.
        finished = true;
    }

    @Override
    public void run() {
        try {
            doYourThing();
        } catch (RuntimeException e) {
            log.error("", e);
            alarmLogger.addAlarm("Fatal error" + ExceptionUtils.getStackTrace(e));
        } catch (InterruptedException e) {
            log.info("Thread interrupted for shutdown.");
            log.debug("", e);
            Thread.currentThread().interrupt();// It doesn't care, just to avoid sonar complaining.
        }

    }

}
