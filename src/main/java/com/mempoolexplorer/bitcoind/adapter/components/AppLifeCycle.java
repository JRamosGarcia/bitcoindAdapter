package com.mempoolexplorer.bitcoind.adapter.components;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.BlockFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.LongPoollingTxPoolFillerImpl;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.AppStateEnum;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.events.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.jobs.MemPoolRefresherJob;
import com.mempoolexplorer.bitcoind.adapter.jobs.MemPoolRefresherJobState;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindAdapterProperties;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindProperties;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.BindingCreatedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

//@Component
//@Profile(value = { AppProfiles.DEV, AppProfiles.PROD })
public class AppLifeCycle {

	private static Logger log = LoggerFactory.getLogger(AppLifeCycle.class);

	@Autowired
	private TxPoolContainer txPoolContainer;

	@Autowired
	private BlockFactory blockFactory;

	@Autowired
	private LongPoollingTxPoolFillerImpl txPoolFiller;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private BitcoindAdapterProperties bitcoindAdapterProperties;

	@Autowired
	private BitcoindProperties bitcoindProperties;

	@Autowired
	private AppState appState;

	@Autowired
	private TxSource txSource;

	@Autowired
	private BitcoindClient bitcoindClient;

	@Autowired
	private BlockTemplateContainer blockTemplateContainer;

	@Autowired
	private AlarmLogger alarmLogger;

	// It seems that Spring aplicaton events are thrown more than once, so these are
	// the flags to avoid calling clean-up methods more than once.
	private boolean hasInitializated = false;// Avoids intialization more than once
	private boolean onApplicationReadyEvent = false;
	private boolean onBindingCreatedEvent = false;
	private boolean onContextClosedEvent = false;

	@Value("${spring.cloud.stream.bindings.txMemPoolEvents.destination}")
	private String topic;

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReadyEvent(ApplicationReadyEvent event) throws SchedulerException, TxPoolException {
		onApplicationReadyEvent = true;
		checkInitialization();
	}

	@EventListener(BindingCreatedEvent.class)
	public void onBindingCreatedEvent(BindingCreatedEvent event) throws SchedulerException, TxPoolException {
		@SuppressWarnings("unchecked")
		Binding<Object> binding = (Binding<Object>) event.getSource();
		// Checks that event.source is the same as our kafka topic
		if (binding.getName().compareTo(topic) == 0) {
			onBindingCreatedEvent = true;
			checkInitialization();
		}
	}

	// @PreDestroy This is not good, better use this:
	@EventListener(ContextClosedEvent.class)
	public void finalization() {
		if (!onContextClosedEvent) {
			onContextClosedEvent = true;
			log.info("Shuting down bitcoindAdapter scheduler...");
			try {
				scheduler.shutdown();
			} catch (SchedulerException e) {
				log.error("Error shutting down scheduller, exception: ", e);
			}
			log.info("BitcoindAdapter scheduler shutdown complete.");
		}
	}

	public void checkInitialization() throws SchedulerException, TxPoolException {
		if (onApplicationReadyEvent && onBindingCreatedEvent && !hasInitializated) {
			hasInitializated = true;
			initialization();
		}
	}

	// @PostConstruct does not hook well. Called when webServices are not
	// initialized or kafka is not binded
	// better hook on an aplicationReadyEvent and BindingCreatedEvent
	public void initialization() throws SchedulerException, TxPoolException {

		log.info("Initializating bitcoindAdapter Mempool...");
		log.info("BitcoindAdapter mempool loading from bitcoind client at ip: {}:{} .It will take sometime",
				bitcoindProperties.getHost(), bitcoindProperties.getRpcPort());
		appState.setState(AppStateEnum.LOADINGFROMBITCOINCLIENT);
		TxPool txPool = txPoolFiller.createMemPool();
		txPoolContainer.setTxPool(txPool);
		appState.setState(AppStateEnum.STARTED);
		log.info("BitcoindAdapter mempool initializated.");
		startJob();
	}

	// In case of SchedulerException we cannot recover so we don't get it
	private void startJob() throws SchedulerException {
		// We are screwing in DB-jobs because Quartz Jobs serialize JobDataMap. But we
		// are not using that feature.
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("memPoolContainer", txPoolContainer);
		jobDataMap.put("bitcoindAdapterProperties", bitcoindAdapterProperties);
		jobDataMap.put("blockFactory", blockFactory);
		jobDataMap.put("txSource", txSource);
		jobDataMap.put("txPoolFiller", txPoolFiller);
		jobDataMap.put("bitcoindClient", bitcoindClient);
		jobDataMap.put("blockTemplateContainer", blockTemplateContainer);
		jobDataMap.put("alarmLogger", alarmLogger);
		jobDataMap.put("state", new MemPoolRefresherJobState());

		JobDetail job = newJob(MemPoolRefresherJob.class).withIdentity("refreshJob", "mempool").setJobData(jobDataMap)
				.build();
		Trigger trigger = newTrigger().withIdentity("refreshTrigger", "mempool").startNow()
				.withSchedule(simpleSchedule().withIntervalInSeconds(bitcoindAdapterProperties.getRefreshIntervalSec())
						.withMisfireHandlingInstructionNowWithRemainingCount().repeatForever())
				.build();

		scheduler.scheduleJob(job, trigger);
		scheduler.start();
	}

}
