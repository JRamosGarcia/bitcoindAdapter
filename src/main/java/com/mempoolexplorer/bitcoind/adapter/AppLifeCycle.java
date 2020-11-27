package com.mempoolexplorer.bitcoind.adapter;

import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.jobs.BlockTemplateRefresherJob;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindAdapterProperties;
import com.mempoolexplorer.bitcoind.adapter.threads.ZMQSequenceEventConsumer;
import com.mempoolexplorer.bitcoind.adapter.threads.ZMQSequenceEventReceiver;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private BitcoindAdapterProperties bitcoindAdapterProperties;

    @Autowired
    private BitcoindClient bitcoindClient;

    @Autowired
    private BlockTemplateContainer blockTemplateContainer;

    @Autowired
    private AlarmLogger alarmLogger;

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
        schedulerShutdown();
        log.info("BitcoindAdapter shutdown complete.");
    }

    private void schedulerShutdown() {
        log.info("Shuting down bitcoindAdapter scheduler...");
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            log.error("Error shutting down scheduller, exception: ", e);
        }
        log.info("BitcoindAdapter scheduler shutdown complete.");
    }

    public void checkInitialization() throws SchedulerException, TxPoolException {
        if (onApplicationReadyEvent && onBindingCreatedEvent && !hasInitializated) {
            hasInitializated = true;
            initialization();
        }
    }

    private void initialization() throws SchedulerException {
        log.info("bitcoinAdapter ZMQ receiver and consumer are starting...");
        // We keep the blockingQueue private among producer and consumer.
        // No size limit. Should be enough fast to not get "full"
        zmqSequenceEventConsumer.start();
        zmqSequenceEventReceiver.start();
        log.info("BitcoinAdapter ZMQ receiver and consumer started.");
        startBlockTemplateRefresherJob();
        log.info("BlockTemplateRefresherJob started.");
    }

    // In case of SchedulerException we cannot recover so we don't get it
    private void startBlockTemplateRefresherJob() throws SchedulerException {
        // We are screwing in DB-jobs because Quartz Jobs serialize JobDataMap. But we
        // are not using that feature.
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("bitcoindClient", bitcoindClient);
        jobDataMap.put("blockTemplateContainer", blockTemplateContainer);
        jobDataMap.put("alarmLogger", alarmLogger);

        JobDetail job = JobBuilder.newJob(BlockTemplateRefresherJob.class)
                .withIdentity("blockTemplateRefresherJob", "blockTemplate").setJobData(jobDataMap).build();
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("blockTemplateRefreshTrigger", "blockTemplate")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(bitcoindAdapterProperties.getRefreshIntervalSec())
                        .withMisfireHandlingInstructionNowWithRemainingCount().repeatForever())
                .build();

        scheduler.scheduleJob(job, trigger);
        scheduler.start();
    }

}
