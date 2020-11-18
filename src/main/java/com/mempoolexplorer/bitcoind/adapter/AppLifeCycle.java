package com.mempoolexplorer.bitcoind.adapter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.threads.MempoolSeqEvent;
import com.mempoolexplorer.bitcoind.adapter.threads.ZMQSequenceEventConsumer;
import com.mempoolexplorer.bitcoind.adapter.threads.ZMQSequenceEventReceiver;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.BindingCreatedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * This is the main class where everything is started, uses spring events to
 * know when to start or end the application gracefully. Listen for
 * ApplicationReadyEvent and BindingCreatedEvent to start application,
 * ContextClosedEvent to shutdown. I think this is a better approach
 * to @DependsOn or @Order
 */
@Component
@Profile(value = { AppProfiles.DEV, AppProfiles.PROD })
@Slf4j
public class AppLifeCycle {

    // It seems that Spring aplicaton events are thrown more than once, so these are
    // the flags to avoid calling clean-up methods more than once.
    private boolean hasInitializated = false;// Avoids intialization more than once
    private boolean isShutingdown = false; // Avoids finalization more than once
    private boolean onApplicationReadyEvent = false;
    private boolean onBindingCreatedEvent = false;

    // Kafka topic
    @Value("${spring.cloud.stream.bindings.txMemPoolEvents.destination}")
    private String topic;

    @Autowired
    private ZMQSequenceEventReceiver zmqSequenceEventReceiver;

    @Autowired
    private ZMQSequenceEventConsumer zmqSequenceEventConsumer;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) throws SchedulerException, TxPoolException {
        onApplicationReadyEvent = true;
        checkInitialization();
    }

    // Sent when kafka binding is done. Wait for it since we don't want to send
    // things before kafka initialization.
    @EventListener(BindingCreatedEvent.class)
    public void onBindingCreatedEvent(BindingCreatedEvent event) throws SchedulerException, TxPoolException {
        @SuppressWarnings("unchecked") // Since we are receving this event we know it's type
        Binding<Object> binding = (Binding<Object>) event.getSource();
        // Checks that event.source is the same as our kafka topic
        if (binding.getName().compareTo(topic) == 0) {
            onBindingCreatedEvent = true;
            checkInitialization();
        }
    }

    // @PreDestroy <- This is not good, better use this:
    @EventListener(ContextClosedEvent.class)
    public void finalization() {
        if (isShutingdown)
            return;// No more than once
        isShutingdown = true;
        log.info("Shuttingdown bitcoindAdapter...");
        zmqSequenceEventConsumer.shutdown();
        zmqSequenceEventReceiver.shutdown();
        log.info("BitcoindAdapter shutdown complete.");
    }

    public void checkInitialization() throws SchedulerException, TxPoolException {
        if (onApplicationReadyEvent && onBindingCreatedEvent && !hasInitializated) {
            hasInitializated = true;
            initialization();
        }
    }

    private void initialization() {
        log.info("bitcoinAdapter ZMQ receiver and consumer are starting...");
        // We keep the blockingQueue private among producer and consumer.
        BlockingQueue<MempoolSeqEvent> blockingQueue = new LinkedBlockingQueue<>();
        zmqSequenceEventConsumer.setBlockingQueue(blockingQueue);
        zmqSequenceEventReceiver.setBlockingQueue(blockingQueue);
        zmqSequenceEventConsumer.start();
        zmqSequenceEventReceiver.start();
        log.info("bitcoinAdapter ZMQ receiver and consumer started.");
    }

}
