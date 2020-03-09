package com.mempoolexplorer.bitcoind.adapter.components;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Optional;

import javax.annotation.PreDestroy;

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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.AppProfiles;
import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.blockchain.changes.LastBlocksContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.components.containers.txpool.changes.TxPoolChangesContainer;
import com.mempoolexplorer.bitcoind.adapter.components.factories.BlockFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.InMemTxPoolFillerImpl;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.TxPoolException;
import com.mempoolexplorer.bitcoind.adapter.entities.AppStateEnum;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPool;
import com.mempoolexplorer.bitcoind.adapter.events.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.jobs.MemPoolRefresherJob;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindAdapterProperties;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindProperties;
import com.mempoolexplorer.bitcoind.adapter.services.TxPoolService;

@Component
@Profile(value = { AppProfiles.DEV, AppProfiles.PROD })
public class AppLifeCycle implements ApplicationListener<ApplicationEvent> {

	private static Logger log = LoggerFactory.getLogger(AppLifeCycle.class);

	@Autowired
	private TxPoolContainer txPoolContainer;

	@Autowired
	private LastBlocksContainer lastBlocksContainer;

	@Autowired
	private TxPoolChangesContainer txPoolChangesContainer;

	@Autowired
	private BlockFactory blockFactory;

	@Autowired
	private InMemTxPoolFillerImpl inMemTxPoolFiller;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private TxPoolService memPoolService;

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

	private boolean hasInitializated = false;// Avoids intialization more than once

	private boolean onApplicationReadyEvent = false;

	private boolean onBindingCreatedEvent = false;

	@Value("${spring.cloud.stream.bindings.txMemPoolEvents.destination}")
	private String topic;

	private enum LoadedFrom {
		FROMDB, FROMCLIENT

	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReadyEvent(ApplicationReadyEvent event) {
		onApplicationReadyEvent = true;
		checkInitialization();
	}

	@EventListener(BindingCreatedEvent.class)
	public void OnBindingCreatedEvent(BindingCreatedEvent event) {
		@SuppressWarnings("unchecked")
		Binding<Object> binding = (Binding<Object>) event.getSource();
		// Checks that event.source is the same as our kafka topic
		if (binding.getName().compareTo(topic) == 0) {
			onBindingCreatedEvent = true;
			checkInitialization();
		}
	}

	public void checkInitialization() {
		if (onApplicationReadyEvent && onBindingCreatedEvent) {
			if (!hasInitializated) {
				hasInitializated = true;
				initialization();
			}
		}
	}

	// @PostConstruct does not hook well. Called when webServices are not
	// initialized. better hook on an aplicationReadyEvent.
	// SpringApplicationBuilder fires multiple notification to that listener (for
	// every child)
	// if (event.getApplicationContext().getParent() == null) {
	// aplicationReadyEvent is called before Kafka binding is done
	// So above we check for ApplicationReadyEvent and BindingCreatedEvent
	public void initialization() {

		log.info("Initializating bitcoindAdapter Mempool...");
		try {
			Optional<TxPool> memPoolFromDB = Optional.empty();
			if (bitcoindAdapterProperties.getLoadDBOnStart()) {
				appState.setState(AppStateEnum.LOADINGFROMDB);
				memPoolFromDB = memPoolService.loadTxPoolFromDB();
			}

			LoadedFrom loadedFrom = LoadedFrom.FROMDB;
			if (memPoolFromDB.isPresent()) {
				log.info("BitcoindAdapter mempool loaded from DB.");
				txPoolContainer.setTxPool(memPoolFromDB.get());
				loadedFrom = LoadedFrom.FROMDB;
			} else {
				log.info("BitcoindAdapter mempool loading from bitcoind client at ip: {}:{} .It will take sometime",
						bitcoindProperties.getHost(), bitcoindProperties.getRpcPort());
				appState.setState(AppStateEnum.LOADINGFROMBITCOINCLIENT);
				TxPool txPool = inMemTxPoolFiller.createMemPool();
				txPoolContainer.setTxPool(txPool);
				loadedFrom = LoadedFrom.FROMCLIENT;
			}
			// It's a nonsense save in DB whatever you have just loaded.
			if (bitcoindAdapterProperties.getSaveDBOnStart() && loadedFrom.equals(LoadedFrom.FROMCLIENT)) {
				log.info("BitcoindAdapter mempool loaded. Now saving to DB.");
				// TODO: maybe this takes too long and db connection closes after timeout
				appState.setState(AppStateEnum.SAVINGTODB);
				memPoolService.saveAllMemPool(txPoolContainer.getTxPool());
				log.info("BitcoindAdapter mempool saved to DB.");
			} else {
				log.info("BitcoindAdapter mempool loaded. NOT saving to DB.");
			}

			appState.setState(AppStateEnum.STARTED);

			log.info("BitcoindAdapter mempool initializated.");
			startJob();
		} catch (TxPoolException e) {
			// log.error("Exception creating initial memPool: ", e);
			throw new RuntimeException("Exception creating initial memPool: ", e);// We must not recover from this
		} catch (SchedulerException e) {
			// log.error("Exception Starting scheduler: ", e);
			throw new RuntimeException("Exception creating initial memPool: ", e);// We must not recover from this
		}

	}

	private void startJob() throws SchedulerException {
		// We are screwing in DB-jobs because Quartz Jobs serialize JobDataMap. But we
		// are not using that feature.
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put("memPoolContainer", txPoolContainer);
		jobDataMap.put("lastBlocksContainer", lastBlocksContainer);
		jobDataMap.put("memPoolChangesContainer", txPoolChangesContainer);
		jobDataMap.put("bitcoindAdapterProperties", bitcoindAdapterProperties);
		jobDataMap.put("memPoolService", memPoolService);
		jobDataMap.put("blockFactory", blockFactory);
		jobDataMap.put("txSource", txSource);
		jobDataMap.put("txPoolFiller", inMemTxPoolFiller);
		jobDataMap.put("bitcoindClient", bitcoindClient);
		jobDataMap.put("blockTemplateContainer", blockTemplateContainer);
		jobDataMap.put("alarmLogger", alarmLogger);

		JobDetail job = newJob(MemPoolRefresherJob.class).withIdentity("refreshJob", "mempool").setJobData(jobDataMap)
				.build();
		Trigger trigger = newTrigger().withIdentity("refreshTrigger", "mempool").startNow()
				.withSchedule(simpleSchedule().withIntervalInSeconds(bitcoindAdapterProperties.getRefreshIntervalSec())
						.withMisfireHandlingInstructionNowWithRemainingCount().repeatForever())
				.build();

		scheduler.scheduleJob(job, trigger);
		scheduler.start();
	}

	@PreDestroy
	public void finalization() {
		log.info("Finalizing bitcoindAdapter Mempool...");
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			log.error("Error shutting down scheduller, exception: ", e);
		}
		log.info("BitcoindAdapter mempool finalized.");
	}

	// Just for debug.
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
//		String str = event.toString();
//		if (str.contains("kafka") || str.contains("cloud")) {
//			log.info("ApplicationEvent: " + event.toString());
//		}
	}
}
