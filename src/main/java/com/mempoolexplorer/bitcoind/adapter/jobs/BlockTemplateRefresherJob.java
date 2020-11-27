package com.mempoolexplorer.bitcoind.adapter.jobs;

import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResult;
import com.mempoolexplorer.bitcoind.adapter.bitcoind.entities.results.GetBlockTemplateResultData;
import com.mempoolexplorer.bitcoind.adapter.components.alarms.AlarmLogger;
import com.mempoolexplorer.bitcoind.adapter.components.clients.BitcoindClient;
import com.mempoolexplorer.bitcoind.adapter.components.containers.blocktemplate.BlockTemplateContainer;
import com.mempoolexplorer.bitcoind.adapter.entities.blocktemplate.BlockTemplate;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This job only updates blockTemplateContainer with new TemplateBlocks
 */
@Getter
@Setter
@DisallowConcurrentExecution
@Slf4j
public class BlockTemplateRefresherJob implements Job {

    private BitcoindClient bitcoindClient;
    private BlockTemplateContainer blockTemplateContainer;
    private AlarmLogger alarmLogger;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            GetBlockTemplateResult blockTemplateResult = bitcoindClient.getBlockTemplateResult();
            if (blockTemplateResult.getError() != null) {
                alarmLogger.addAlarm("Can't get block template result. Maybe bitcoind is down? Error: "
                        + blockTemplateResult.getError());
                log.error("Can't get block template result. Maybe bitcoind is down? Error: {}",
                        blockTemplateResult.getError());
                return;
            }
            GetBlockTemplateResultData getBlockTemplateResultData = blockTemplateResult.getGetBlockTemplateResultData();
            BlockTemplate newBT = new BlockTemplate(getBlockTemplateResultData);
            blockTemplateContainer.setNewestBlockTemplate(newBT);
            log.debug("New blockTemplate arrived from bitcoind");

        } catch (RuntimeException e) {
            alarmLogger.addAlarm("Exception: " + e.getMessage());
            log.error("Exception: ", e);
        }
    }

}
