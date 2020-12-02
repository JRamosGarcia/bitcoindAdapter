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
import org.springframework.web.client.ResourceAccessException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This job only updates blockTemplateContainer with new TemplateBlocks
 */
@Getter
@Setter
@NoArgsConstructor
@DisallowConcurrentExecution
@Slf4j
public class BlockTemplateRefresherJob implements Job {

    private BitcoindClient bitcoindClient;
    private BlockTemplateContainer blockTemplateContainer;
    private AlarmLogger alarmLogger;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // This loop is for be sure that
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
            blockTemplateContainer.push(newBT);
            log.debug("New blockTemplate arrived from bitcoind");

        } catch (ResourceAccessException e) {
            log.error("Seems bitcoind is down {}", e.getMessage());
            alarmLogger.addAlarm("Seems bitcoind is down." + e.getMessage());
        } catch (RuntimeException e) {
            alarmLogger.addAlarm("Exception: " + e.getMessage());
            log.error("Exception: ", e);
        }
    }

}
