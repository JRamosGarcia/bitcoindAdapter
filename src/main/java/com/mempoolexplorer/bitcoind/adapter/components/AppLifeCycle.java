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
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.mempoolexplorer.bitcoind.adapter.AppProfiles;
import com.mempoolexplorer.bitcoind.adapter.components.factories.TxPoolChangesFactory;
import com.mempoolexplorer.bitcoind.adapter.components.factories.exceptions.MemPoolException;
import com.mempoolexplorer.bitcoind.adapter.components.mempoolcontainers.changes.TxPoolChangesContainer;
import com.mempoolexplorer.bitcoind.adapter.components.sources.TxSource;
import com.mempoolexplorer.bitcoind.adapter.components.txpoolcontainers.TxPoolContainer;
import com.mempoolexplorer.bitcoind.adapter.entities.AppStateEnum;
import com.mempoolexplorer.bitcoind.adapter.entities.mempool.TxPoolDiff;
import com.mempoolexplorer.bitcoind.adapter.jobs.MemPoolRefresherJob;
import com.mempoolexplorer.bitcoind.adapter.properties.BitcoindAdapterProperties;
import com.mempoolexplorer.bitcoind.adapter.services.TxPoolService;

@Component
@Profile(value = { AppProfiles.DEV, AppProfiles.PROD })
public class AppLifeCycle /* implements ApplicationListener<ApplicationEvent> */ {

	private static Logger log = LoggerFactory.getLogger(AppLifeCycle.class);

	@Autowired
	private TxPoolContainer txPoolContainer;

	@Autowired
	private TxPoolChangesContainer txPoolChangesContainer;

	@Autowired
	private TxPoolChangesFactory txPoolChangesFactory;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private TxPoolService memPoolService;

	@Autowired
	private BitcoindAdapterProperties props;

	@Autowired
	private AppState appState;

	@Autowired
	private TxSource txSource;

	private Boolean applicationReadyEventFirstTime = Boolean.TRUE;
	
//	@SuppressWarnings("deprecation")
//	private Boolean firstMemPoolRefresh = new Boolean(true);

	private enum LoadedFrom {
		FROMDB, FROMCLIENT

	}

	// @PostConstruct does not hook well. Called when webServices are not
	// initialized. better hook on an aplicationReadyEvent.
	@EventListener(ApplicationReadyEvent.class)
	public void initialization(ApplicationReadyEvent event) {
		// SpringApplicationBuilder fires multiple notification to that listener (for
		// every child)
		//if (event.getApplicationContext().getParent() == null) {
		//NO funciona lo anterior, haciendo chapuza...
		if(applicationReadyEventFirstTime) {
			applicationReadyEventFirstTime=Boolean.FALSE;

			// TODO Do this process more resilient in case bitcoind is not started.
			log.info("Initializating bitcoindAdapter Mempool...");
			try {
				Optional<TxPoolDiff> memPoolFromDB = Optional.empty();
				if (props.getLoadDBOnStart()) {
					appState.setState(AppStateEnum.LOADINGFROMDB);
					memPoolFromDB = memPoolService.loadTxPoolFromDB();
				}

				LoadedFrom loadedFrom = LoadedFrom.FROMDB;
				if (memPoolFromDB.isPresent()) {
					log.info("BitcoindAdapter mempool loaded from DB.");
					txPoolContainer.getTxPool().apply(memPoolFromDB.get());
					loadedFrom = LoadedFrom.FROMDB;
				} else {
					log.info("BitcoindAdapter mempool loading from bitcoind client. It will take sometime");
					appState.setState(AppStateEnum.LOADINGFROMBITCOINCLIENT);
					txPoolContainer.createTxPool();
					loadedFrom = LoadedFrom.FROMCLIENT;
				}
				// It's a nonsense save in DB whatever you have just loaded.
				if (props.getSaveDBOnStart() && loadedFrom.equals(LoadedFrom.FROMCLIENT)) {
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

				// We are screwing in DB-jobs because Quartz Jobs serialize JobDataMap. But we
				// are not using that feature.
				JobDataMap jobDataMap = new JobDataMap();
				jobDataMap.put("memPoolContainer", txPoolContainer);
				jobDataMap.put("memPoolChangesContainer", txPoolChangesContainer);
				jobDataMap.put("saveDBOnRefresh", props.getSaveDBOnRefresh());
				jobDataMap.put("memPoolService", memPoolService);
				jobDataMap.put("memPoolChangesFactory", txPoolChangesFactory);
				jobDataMap.put("txSource", txSource);
//				jobDataMap.put("firstMemPoolRefresh", firstMemPoolRefresh);
				
				JobDetail job = newJob(MemPoolRefresherJob.class).withIdentity("refreshJob", "mempool")
						.setJobData(jobDataMap).build();
				Trigger trigger = newTrigger().withIdentity("refreshTrigger", "mempool").startNow()
						.withSchedule(simpleSchedule().withIntervalInSeconds(props.getRefreshIntervalSec())
								.withMisfireHandlingInstructionNowWithRemainingCount().repeatForever())
						.build();

				scheduler.scheduleJob(job, trigger);
				scheduler.start();
			} catch (MemPoolException e) {
				log.error("Exception creating initial memPool: ", e);
			} catch (SchedulerException e) {
				log.error("Exception Starting scheduler: ", e);
			}
		}

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

	// // Just for debug.
	// @Override
	// public void onApplicationEvent(ApplicationEvent event) {
	// log.info("ApplicationEvent: " + event.toString());
	//
	// }
}
